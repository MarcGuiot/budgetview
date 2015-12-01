package org.designup.picsou.gui.series.upgrade;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.accounts.utils.AccountMatchers;
import org.designup.picsou.gui.description.stringifiers.AccountComparator;
import org.designup.picsou.gui.notifications.standard.StandardMessageNotificationHandler;
import org.designup.picsou.gui.transactions.utils.MirrorTransactionFinder;
import org.designup.picsou.gui.upgrade.BindTransactionsToSeries;
import org.designup.picsou.gui.upgrade.PostProcessor;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    GlobList accounts = repository.getAll(Account.TYPE, AccountMatchers.userCreatedMainAccounts()).sortSelf(new AccountComparator());
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
      repository.update(series, Series.TARGET_ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID);
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
      Integer[] accountIds1 = getAccounts(transactions1);
      Integer[] accountIds2 = getAccounts(transactions2);
      if ((accountIds1.length > 1) && (accountIds2.length == 0)) {
        tryToRepairTransactionSeries(series1, transactions1, accountIds1, series2, transactions2, accountIds2);
      }
      else if (((accountIds1.length == 0) && (accountIds2.length > 1))) {
        tryToRepairTransactionSeries(series2, transactions2, accountIds2, series1, transactions1, accountIds1);
      }

    }
      for (Map.Entry<Key, Key> entry : savings.entrySet()) {
      Glob series1 = repository.get(entry.getKey());
      Glob series2 = repository.get(entry.getValue());
      GlobList transactions1 = getTransactions(series1, repository);
      GlobList transactions2 = getTransactions(series2, repository);
      Integer[] accountIds1 = getAccounts(transactions1);
      Integer[] accountIds2 = getAccounts(transactions2);
      if ((accountIds1.length <= 1) && (accountIds2.length <= 1)) {
        upgradeTransfer(series1, series2, transactions1, transactions2, accountIds1, accountIds2, repository);
      }
      else if ((accountIds1.length > 1) && (accountIds2.length == 1)) {
        rebuildTransfer(series1, transactions1, accountIds1, series2, transactions2, accountIds2);
      }
      else if (((accountIds1.length == 1) && (accountIds2.length > 1))) {
        rebuildTransfer(series2, transactions2, accountIds2, series1, transactions1, accountIds1);
      }
      else { // Should never occur - clean up the situation if it does happen
        StandardMessageNotificationHandler.notify(Lang.get("upgrade.v40.multiAccountSavingsSeriesDeleted", series1.get(Series.NAME)), repository);
        Series.delete(series1, repository);
      }
    }
  }

  private void upgradeTransfer(Glob series1, Glob series2, GlobList transactions1, GlobList transactions2, Integer[] accountIds1, Integer[] accountIds2, GlobRepository repository) {
    Integer target1 = setTarget(series1, accountIds1);
    Integer target2 = setTarget(series2, accountIds2);
    if ((target1 == null) || (target2 == null)) {
      StandardMessageNotificationHandler.notify(Lang.get("upgrade.v40.noTransactionsSavingsSeriesDeleted", series1.get(Series.NAME)), repository);
      Series.delete(series1, repository);
      return;
    }

    if (isSeries1Debit(series1, transactions1, target1, series2, transactions2, target2)) {
      repository.update(series1,
                        value(Series.FROM_ACCOUNT, target1),
                        value(Series.TO_ACCOUNT, target2));
      repository.update(series2,
                        value(Series.FROM_ACCOUNT, target1),
                        value(Series.TO_ACCOUNT, target2));
    }
    else {
      repository.update(series1,
                        value(Series.FROM_ACCOUNT, target2),
                        value(Series.TO_ACCOUNT, target1));
      repository.update(series2,
                        value(Series.FROM_ACCOUNT, target2),
                        value(Series.TO_ACCOUNT, target1));
    }

    postProcessor.add(new BindTransactionsToSeries(series1, transactions1));
    postProcessor.add(new BindTransactionsToSeries(series2, transactions2));
  }

  private Integer setTarget(Glob series, Integer[] accountIds) {
    if (accountIds.length == 1) {
      repository.update(series.getKey(), Series.TARGET_ACCOUNT, accountIds[0]);
      return accountIds[0];
    }
    Integer existingTargetAccountId = series.get(Series.TARGET_ACCOUNT);
    if (Account.isUserCreatedAccount(existingTargetAccountId)) {
      return existingTargetAccountId;
    }
    if (defaultMainAccount == null) {
      return null;
    }
    if (!singleMainAccount) {
      StandardMessageNotificationHandler.notify(Lang.get("upgrade.v40.noTransactionsSavingsSeriesAdjusted", series.get(Series.NAME), defaultMainAccount.get(Account.NAME)), repository);
    }
    Integer accountId = defaultMainAccount.get(Account.ID);
    repository.update(series.getKey(), Series.TARGET_ACCOUNT, accountId);
    return accountId;
  }

  private boolean isSeries1Debit(Glob series1, GlobList transactions1, Integer targetAccount1, Glob series2, GlobList transactions2, Integer targetAccount2) {
    if (transactions1.size() > 0) {
      return transactions1.getFirst().get(Transaction.AMOUNT) < 0;
    }
    if (transactions2.size() > 0) {
      return transactions2.getFirst().get(Transaction.AMOUNT) > 0;
    }
    return Utils.equal(series1.get(Series.FROM_ACCOUNT), targetAccount1) ||
           Utils.equal(series1.get(Series.TO_ACCOUNT), targetAccount2) ||
           Utils.equal(series2.get(Series.FROM_ACCOUNT), targetAccount2) ||
           Utils.equal(series2.get(Series.TO_ACCOUNT), targetAccount1);
  }

  private void tryToRepairTransactionSeries(Glob multiSeries, GlobList multiTransactions, Integer[] multiAccountIds, Glob monoSeries, GlobList monoTransactions, Integer[] monoAccountIds){
    if (monoAccountIds.length == 0) {
      if (multiTransactions.getValueSet(Transaction.MIRROR).size() == 2) {
        // on a un bug : la meme series porte les operations mirroir et normal.
        int negativeSeries;
        int positiveSeries;
        if (multiSeries.get(Series.TARGET_ACCOUNT).equals(multiSeries.get(Series.FROM_ACCOUNT))) {
          negativeSeries = multiSeries.get(Series.ID);
          positiveSeries = monoSeries.get(Series.ID);
        }
        else {
          negativeSeries = monoSeries.get(Series.ID);
          positiveSeries = multiSeries.get(Series.ID);
        }
        for (Glob transaction : multiTransactions) {
          if (transaction.get(Transaction.AMOUNT) >= 0) {
            repository.update(transaction.getKey(), Transaction.SERIES, positiveSeries);
          }
          else {
            repository.update(transaction.getKey(), Transaction.SERIES, negativeSeries);
          }
        }
      }
    }
  }

  private void rebuildTransfer(Glob multiSeries, GlobList multiTransactions, Integer[] multiAccountIds, Glob monoSeries, GlobList monoTransactions, Integer[] monoAccountIds) {
    StandardMessageNotificationHandler.notify(Lang.get("upgrade.v40.multiAccountSavingsSeriesSplitted", multiSeries.get(Series.NAME)), repository);
    for (int i = 0; i < multiAccountIds.length; i++) {
      Integer accountId = multiAccountIds[i];
      GlobList accountTransactions = getAccountTransactions(multiTransactions, accountId);
      Integer monoAccountId = monoAccountIds[0];
      GlobList mirrorTransactions = extractMirrorTransactions(accountTransactions, monoAccountId, monoTransactions);
      FieldValuesBuilder valuesBuilder = FieldValuesBuilder.init(multiSeries)
        .remove(Series.ID)
        .remove(Series.TARGET_ACCOUNT)
        .remove(Series.FROM_ACCOUNT)
        .remove(Series.TO_ACCOUNT);
      CreateTransferSeries createTransfer = new CreateTransferSeries(multiSeries.get(Series.NAME), valuesBuilder, accountId, accountTransactions, monoAccountId, mirrorTransactions);
      for (Glob seriesBudget : repository.findLinkedTo(multiSeries, SeriesBudget.SERIES)) {
        Integer monthId = seriesBudget.get(SeriesBudget.MONTH);
        double actualAmount = accountTransactions.filter(fieldEquals(Transaction.BUDGET_MONTH, monthId), repository).getSum(Transaction.AMOUNT, 0.00);
        double[] plannedAmounts = Amounts.split(seriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0.00), multiAccountIds.length);
        createTransfer.addBudget(monthId, actualAmount, plannedAmounts[i], seriesBudget.get(SeriesBudget.ACTIVE, false));
      }
      postProcessor.add(createTransfer);
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

  private Integer[] getAccounts(GlobList transactions) {
    return transactions.getValueSetArray(Transaction.ACCOUNT);
  }

  private GlobList getTransactions(Glob series, GlobRepository repository) {
    return repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID))
      .getGlobs().filter(GlobMatchers.isFalse(Transaction.PLANNED), repository);
  }

  private class CreateTransferSeries implements PostProcessor.Functor {
    private String baseName;
    private final FieldValuesBuilder valuesBuilder;
    private final Integer accountId;
    private final GlobList accountTransactions;
    private Integer mirrorAccountId;
    private final GlobList mirrorTransactions;
    private List<Budget> budgetList = new ArrayList<Budget>();

    public CreateTransferSeries(String baseName, FieldValuesBuilder valuesBuilder, Integer accountId, GlobList accountTransactions, Integer mirrorAccountId, GlobList mirrorTransactions) {
      this.baseName = baseName;
      this.valuesBuilder = valuesBuilder;
      this.accountId = accountId;
      this.accountTransactions = accountTransactions;
      this.mirrorAccountId = mirrorAccountId;
      this.mirrorTransactions = mirrorTransactions;
    }

    public void apply(GlobRepository repository) {
      boolean positive = accountTransactions.getFirst().get(Transaction.AMOUNT) >= 0;
      Glob series = repository.create(Series.TYPE,
                                      valuesBuilder
                                        .set(Series.NAME, getSeriesName(repository))
                                        .set(Series.TARGET_ACCOUNT, accountId)
                                        .set(Series.FROM_ACCOUNT, positive ? mirrorAccountId : accountId)
                                        .set(Series.TO_ACCOUNT, positive ? accountId : mirrorAccountId)
                                        .toArray());
      Glob mirror = Series.createMirror(series, mirrorAccountId, repository);
      for (Budget budget : budgetList) {
        repository.create(SeriesBudget.TYPE,
                          value(SeriesBudget.SERIES, series.get(Series.ID)),
                          value(SeriesBudget.MONTH, budget.monthId),
                          value(SeriesBudget.PLANNED_AMOUNT, budget.plannedAmount),
                          value(SeriesBudget.ACTIVE, budget.active));
        repository.create(SeriesBudget.TYPE,
                          value(SeriesBudget.SERIES, mirror.get(Series.ID)),
                          value(SeriesBudget.MONTH, budget.monthId),
                          value(SeriesBudget.PLANNED_AMOUNT, -budget.plannedAmount),
                          value(SeriesBudget.ACTIVE, budget.active));
      }
      BindTransactionsToSeries.run(accountTransactions, series.get(Series.ID), repository);
      BindTransactionsToSeries.run(mirrorTransactions, mirror.get(Series.ID), repository);
    }

    public String getSeriesName(GlobRepository repository) {
      Glob account = repository.get(Key.create(Account.TYPE, accountId));
      return baseName + " (" + account.get(Account.NAME) + ")";
    }

    private class Budget {
      final int monthId;
      final double actualAmount;
      final double plannedAmount;
      private boolean active;

      public Budget(int monthId, double actualAmount, double plannedAmount, Boolean active) {
        this.monthId = monthId;
        this.actualAmount = actualAmount;
        this.plannedAmount = plannedAmount;
        this.active = active;
      }
    }

    public void addBudget(Integer monthId, double actual, double planned, boolean active) {
      budgetList.add(new Budget(monthId, actual, planned, active));
    }
  }
}
