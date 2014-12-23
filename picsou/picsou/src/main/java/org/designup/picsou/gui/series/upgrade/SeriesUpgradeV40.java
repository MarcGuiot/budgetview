package org.designup.picsou.gui.series.upgrade;

import org.designup.picsou.gui.description.stringifiers.AccountComparator;
import org.designup.picsou.gui.notifications.standard.StandardMessageNotificationHandler;
import org.designup.picsou.gui.upgrade.BindTransactionsToSeries;
import org.designup.picsou.gui.upgrade.PostProcessor;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesUpgradeV40 {


  public static void run(GlobRepository repository, PostProcessor postProcessor) {
    SeriesUpgradeV40 upgrade = new SeriesUpgradeV40(repository, postProcessor);
    upgrade.run();
  }

  private GlobRepository repository;
  private PostProcessor postProcessor;
  private final Glob defaultMainAccount;
  private final boolean singleMainAccount;

  private SeriesUpgradeV40(GlobRepository repository, PostProcessor postProcessor) {
    this.repository = repository;
    this.postProcessor = postProcessor;
    GlobList accounts = repository.getAll(Account.TYPE, Account.userCreatedMainAccounts()).sort(new AccountComparator());
    this.defaultMainAccount = accounts.isEmpty() ? null : accounts.getFirst();
    this.singleMainAccount = accounts.size() == 1;
  }

  private void run() {
    upgradeStandardSeries(repository);
    upgradeSavingsSeries(repository);
  }

  private void upgradeStandardSeries(GlobRepository repository) {
    GlobList allSeries = repository.getAll(Series.TYPE,
                                           and(not(fieldEquals(Series.BUDGET_AREA, BudgetArea.UNCATEGORIZED.getId())),
                                               not(fieldEquals(Series.BUDGET_AREA, BudgetArea.TRANSFER.getId())),
                                               not(fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId()))));
    for (Glob series : allSeries) {
      GlobList transactions = getTransactions(series, repository);
      Set<Integer> accounts = transactions.getValueSet(Transaction.ACCOUNT);
      if (accounts.size() == 0) {
        // leave unchanged
      }
      else if (accounts.size() == 1) {
        repository.update(series.getKey(), Series.TARGET_ACCOUNT, accounts.iterator().next());
      }
      else {
        repository.update(series.getKey(), Series.TARGET_ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID);
      }
    }
    for (final Glob series : allSeries) {
      final Integer targetAccount = series.get(Series.TARGET_ACCOUNT);
      if (targetAccount != null) {
        repository.safeApply(Transaction.TYPE,
                             and(fieldEquals(Transaction.PLANNED, true),
                                 fieldEquals(Transaction.SERIES, series.get(Series.ID))),
                             new GlobFunctor() {
                               public void run(Glob glob, GlobRepository repository) throws Exception {
                                 repository.update(glob.getKey(), Transaction.ACCOUNT, targetAccount);
                               }
                             }
        );
      }
    }
  }

  private void upgradeSavingsSeries(GlobRepository repository) {
    GlobList allSavingsSeries = repository.getAll(Series.TYPE, fieldEquals(Series.BUDGET_AREA, BudgetArea.TRANSFER.getId()));
    Map<Key, Key> savings = new HashMap<Key, Key>();
    for (Glob series : allSavingsSeries) {
      if (!savings.containsKey(series.getKey())) {
        Glob target = repository.findLinkTarget(series, Series.MIRROR_SERIES);
        if (target != null) {
          savings.put(target.getKey(), series.getKey());
        }
      }
    }

    for (Map.Entry<Key, Key> entry : savings.entrySet()) {
      Glob series1 = repository.get(entry.getKey());
      Glob series2 = repository.get(entry.getValue());
      Set<Integer> accountIds1 = updateTargetAccount(series1, series2, repository);
      Set<Integer> accountIds2 = updateTargetAccount(series2, series1, repository);
      updateTargetAccountIfNull(series1, series2, repository);
      if (accountIds1.size() > 1 || accountIds2.size() > 1) {
        StandardMessageNotificationHandler.notify(Lang.get("upgrade.v40.multiAccountSavingsSeriesDeleted", series1.get(Series.NAME)), repository);
        Series.delete(series1, repository);
      }
      else if (Utils.equal(series1.get(Series.TARGET_ACCOUNT), Account.MAIN_SUMMARY_ACCOUNT_ID)) {
        setDefaultMainTarget(series1, repository);
      }
      else if (Utils.equal(series2.get(Series.TARGET_ACCOUNT), Account.MAIN_SUMMARY_ACCOUNT_ID)) {
        setDefaultMainTarget(series2, repository);
      }
    }
  }

  private void setDefaultMainTarget(Glob series, GlobRepository repository) {
    if (defaultMainAccount == null) {
      StandardMessageNotificationHandler.notify(Lang.get("upgrade.v40.noTransactionsSavingsSeriesDeleted", series.get(Series.NAME)), repository);
      Series.delete(series, repository);
    }
    else {
      if (!singleMainAccount) {
        StandardMessageNotificationHandler.notify(Lang.get("upgrade.v40.noTransactionsSavingsSeriesAdjusted", series.get(Series.NAME), defaultMainAccount.get(Account.NAME)), repository);
      }
      setMainTargetAccount(series, defaultMainAccount.get(Account.ID), repository);
    }
  }

  private void setMainTargetAccount(Glob series, Integer accountId, GlobRepository repository) {
    repository.update(series.getKey(), Series.TARGET_ACCOUNT, accountId);
    if (Utils.equal(series.get(Series.FROM_ACCOUNT), Account.MAIN_SUMMARY_ACCOUNT_ID)) {
      repository.update(series.getKey(), Series.FROM_ACCOUNT, accountId);
    }
    if (Utils.equal(series.get(Series.TO_ACCOUNT), Account.MAIN_SUMMARY_ACCOUNT_ID)) {
      repository.update(series.getKey(), Series.TO_ACCOUNT, accountId);
    }
  }

  private Set<Integer> updateTargetAccount(Glob series, Glob mirror, GlobRepository repository) {
    Glob targetAccount = repository.findLinkTarget(series, Series.TARGET_ACCOUNT);
    if (!Account.isMain(targetAccount) || Account.isUserCreatedMainAccount(targetAccount)) {
      return Collections.emptySet();
    }
    GlobList transactions = getTransactions(series, repository);
    Set<Integer> accountIds = transactions.getValueSet(Transaction.ACCOUNT);
    if (accountIds.size() == 1) {
      setMainTargetAccount(series, accountIds.iterator().next(), repository);
      postProcessor.add(new BindTransactionsToSeries(series, transactions));
      postProcessor.add(new BindTransactionsToSeries(mirror, getTransactions(mirror, repository)));
    }
    return accountIds;
  }

  private GlobList getTransactions(Glob series, GlobRepository repository) {
    return repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID))
      .getGlobs().filter(GlobMatchers.isFalse(Transaction.PLANNED), repository);
  }

  private void updateTargetAccountIfNull(Glob series1, Glob series2, GlobRepository repository) {
    if (series1.get(Series.TARGET_ACCOUNT) == null || series2.get(Series.TARGET_ACCOUNT) == null) {
      for (Glob seriesBudget : SeriesBudget.getAll(series1, repository)) {
        if (seriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0.00) > 0) {
          repository.update(series1.getKey(), Series.TARGET_ACCOUNT, series1.get(Series.TO_ACCOUNT));
          repository.update(series2.getKey(), Series.TARGET_ACCOUNT, series1.get(Series.FROM_ACCOUNT));
          return;
        }
      }
      for (Glob seriesBudget : SeriesBudget.getAll(series2, repository)) {
        if (seriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0.00) > 0) {
          repository.update(series2.getKey(), Series.TARGET_ACCOUNT, series1.get(Series.TO_ACCOUNT));
          repository.update(series1.getKey(), Series.TARGET_ACCOUNT, series1.get(Series.FROM_ACCOUNT));
          return;
        }
      }
      repository.update(series1.getKey(), Series.TARGET_ACCOUNT, series1.get(Series.TO_ACCOUNT));
      repository.update(series2.getKey(), Series.TARGET_ACCOUNT, series1.get(Series.FROM_ACCOUNT));
    }
  }
}
