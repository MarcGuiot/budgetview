package org.designup.picsou.gui.series.upgrade;

import org.designup.picsou.gui.description.stringifiers.AccountComparator;
import org.designup.picsou.gui.notifications.standard.StandardMessageNotificationHandler;
import org.designup.picsou.gui.transactions.utils.MirrorTransactionFinder;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;
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
      GlobList transactions1 = getTransactions(series1, repository);
      GlobList transactions2 = getTransactions(series2, repository);
      Set<Integer> accountIds1 = getAccounts(transactions1);
      Set<Integer> accountIds2 = getAccounts(transactions2);
      if ((accountIds1.size() <= 1) && (accountIds2.size() <= 1)) {
        updateTargetAccountIfNull(series1, series2);
        if (accountIds1.size() == 1) {
          repository.update(series1.getKey(), Series.TARGET_ACCOUNT, accountIds1.iterator().next());
        }
        if (accountIds2.size() == 1) {
          repository.update(series2.getKey(), Series.TARGET_ACCOUNT, accountIds2.iterator().next());
        }
        if (Utils.equal(series1.get(Series.TARGET_ACCOUNT), Account.MAIN_SUMMARY_ACCOUNT_ID)) {
          setDefaultMainTarget(series1);
        }
        if (Utils.equal(series2.get(Series.TARGET_ACCOUNT), Account.MAIN_SUMMARY_ACCOUNT_ID)) {
          setDefaultMainTarget(series2);
        }
        postProcessor.add(new BindTransactionsToSeries(series1, transactions1));
        postProcessor.add(new BindTransactionsToSeries(series2, transactions2));
      }
      else if ((accountIds1.size() > 1) && (accountIds2.size() <= 1)) {
        rebuildTransfer(series1, transactions1, accountIds1, series2, transactions2, accountIds2);
      }
      else if (((accountIds1.size() <= 1) && (accountIds2.size() > 1))) {
        rebuildTransfer(series2, transactions2, accountIds2, series1, transactions1, accountIds1);
      }
      else { // Should never occur - clean up the situation if it does happen
        StandardMessageNotificationHandler.notify(Lang.get("upgrade.v40.multiAccountSavingsSeriesDeleted", series1.get(Series.NAME)), repository);
        Series.delete(series1, repository);
      }
    }
  }

  private void rebuildTransfer(Glob multiSeries, GlobList multiTransactions, Set<Integer> multiAccountIds, Glob monoSeries, GlobList monoTransactions, Set<Integer> monoAccountIds) {
    StandardMessageNotificationHandler.notify(Lang.get("upgrade.v40.multiAccountSavingsSeriesSplitted", multiSeries.get(Series.NAME)), repository);
    String name = multiSeries.get(Series.NAME);
    for (Integer accountId : multiAccountIds) {
      GlobList accountTransactions = getAccountTransactions(multiTransactions, accountId);
      Integer monoAccountId = monoAccountIds.isEmpty() ? null : monoAccountIds.iterator().next();
      GlobList mirrorTransactions = extractMirrorTransactions(accountTransactions, monoAccountId, monoTransactions);
      postProcessor.add(new CreateTransferSeries(name, accountId, accountTransactions, monoAccountId, mirrorTransactions));
    }
    Series.delete(multiSeries, repository);
    Series.delete(monoSeries, repository);
  }

  private GlobList extractMirrorTransactions(GlobList accountTransactions, Integer monoAccountId, GlobList mirrorTransactions) {
    if (monoAccountId == null) {
      return GlobList.EMPTY;
    }
    GlobList closestMirrors = MirrorTransactionFinder.getClosestMirrors(accountTransactions, monoAccountId, mirrorTransactions, repository);
    mirrorTransactions.removeAll(closestMirrors);
    return closestMirrors;
  }

  private GlobList getAccountTransactions(GlobList multiTransactions, Integer accountId) {
    return multiTransactions.filter(fieldEquals(Transaction.ACCOUNT, accountId), repository);
  }

  private void setDefaultMainTarget(Glob series) {
    if (defaultMainAccount == null) {
      StandardMessageNotificationHandler.notify(Lang.get("upgrade.v40.noTransactionsSavingsSeriesDeleted", series.get(Series.NAME)), repository);
      Series.delete(series, repository);
    }
    else {
      if (!singleMainAccount) {
        StandardMessageNotificationHandler.notify(Lang.get("upgrade.v40.noTransactionsSavingsSeriesAdjusted", series.get(Series.NAME), defaultMainAccount.get(Account.NAME)), repository);
      }
      setMainTargetAccount(series, defaultMainAccount.get(Account.ID));
    }
  }

  private void setMainTargetAccount(Glob series, Integer accountId) {
    repository.update(series.getKey(), Series.TARGET_ACCOUNT, accountId);
    if (Utils.equal(series.get(Series.FROM_ACCOUNT), Account.MAIN_SUMMARY_ACCOUNT_ID)) {
      repository.update(series.getKey(), Series.FROM_ACCOUNT, accountId);
    }
    if (Utils.equal(series.get(Series.TO_ACCOUNT), Account.MAIN_SUMMARY_ACCOUNT_ID)) {
      repository.update(series.getKey(), Series.TO_ACCOUNT, accountId);
    }
  }

  private Set<Integer> getAccounts(GlobList transactions) {
    return transactions.getValueSet(Transaction.ACCOUNT);
  }

  private GlobList getTransactions(Glob series, GlobRepository repository) {
    return repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID))
      .getGlobs().filter(GlobMatchers.isFalse(Transaction.PLANNED), repository);
  }

  private void updateTargetAccountIfNull(Glob series1, Glob series2) {
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

  private class CreateTransferSeries implements PostProcessor.Functor {
    private final String baseName;
    private final Integer accountId;
    private final GlobList accountTransactions;
    private Integer mirrorAccountId;
    private final GlobList mirrorTransactions;

    public CreateTransferSeries(String baseName, Integer accountId, GlobList accountTransactions, Integer mirrorAccountId, GlobList mirrorTransactions) {
      this.baseName = baseName;
      this.accountId = accountId;
      this.accountTransactions = accountTransactions;
      this.mirrorAccountId = mirrorAccountId;
      this.mirrorTransactions = mirrorTransactions;
    }

    public void apply(GlobRepository repository) {
      boolean positive = accountTransactions.getFirst().get(Transaction.AMOUNT) >= 0;
      Glob series = repository.create(Series.TYPE,
                                      value(Series.NAME, getSeriesName(repository)),
                                      value(Series.BUDGET_AREA, BudgetArea.TRANSFER.getId()),
                                      value(Series.TARGET_ACCOUNT, accountId),
                                      value(Series.FROM_ACCOUNT, positive ? mirrorAccountId : accountId),
                                      value(Series.TO_ACCOUNT, positive ? accountId : mirrorAccountId));
      Glob mirror = Series.createMirror(series, mirrorAccountId, repository);
      BindTransactionsToSeries.run(accountTransactions, series.get(Series.ID), repository);
      BindTransactionsToSeries.run(mirrorTransactions, mirror.get(Series.ID), repository);
    }

    public String getSeriesName(GlobRepository repository) {
      Glob account = repository.get(Key.create(Account.TYPE, accountId));
      return baseName + " (" + account.get(Account.NAME) + ")";
    }
  }
}
