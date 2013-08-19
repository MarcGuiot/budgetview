package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/*
 TODO: updater ce trigger en sachant qu'il y a une operation de debut et une de fin + verifier que
 TODO: absoluteFirstTransaction a un sens (cf Account.FIRST_POSITION)
 */

public class BudgetStatTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE) || changeSet.containsChanges(SeriesBudget.TYPE)
        || changeSet.containsUpdates(Series.BUDGET_AREA)) {
      computeStat(repository);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    computeStat(repository);
  }

  private void computeStat(GlobRepository repository) {
    repository.startChangeSet();
    try {
      repository.deleteAll(BudgetStat.TYPE);
      BudgetStatComputer budgetStatComputer = new BudgetStatComputer(repository);
      if (budgetStatComputer.currentMonth == null) {
        return;
      }
      repository.safeApply(Transaction.TYPE,
                           GlobMatchers.not(GlobMatchers.fieldEquals(Transaction.ACCOUNT, Account.EXTERNAL_ACCOUNT_ID)), budgetStatComputer);
      budgetStatComputer.complete();
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private class BudgetStatComputer implements GlobFunctor {
    private Map<Integer, Glob> firstTransactionForMonth = new HashMap<Integer, Glob>();
    private Map<Integer, Glob> lastTransactionForMonth = new HashMap<Integer, Glob>();
    private Map<Integer, Double> minPosition = new HashMap<Integer, Double>();
    private Map<BudgetArea, BudgetAreaAmounts> budgetAreaAmounts = new HashMap<BudgetArea, BudgetAreaAmounts>();
    private Glob lastRealKnownTransaction;
    private Glob currentMonth;
    private Glob absoluteFirstTransaction;

    private GlobRepository repository;

    private BudgetStatComputer(GlobRepository repository) {
      this.repository = repository;
      currentMonth = repository.find(CurrentMonth.KEY);
    }

    public void run(Glob transaction, GlobRepository repository) throws Exception {
      if (transaction.get(Transaction.SUMMARY_POSITION) == null) {
        Log.write("Summary position is null for transaction : " + transaction.get(Transaction.ID) +
                  " " + transaction.get(Transaction.LABEL));
      }

      Glob account = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
      if (account == null || !AccountType.MAIN.getId().equals(account.get(Account.ACCOUNT_TYPE))) {
        return;
      }

      Integer monthId = transaction.get(Transaction.POSITION_MONTH);

      Double currentPosition = transaction.get(Transaction.SUMMARY_POSITION);
      Double min = minPosition.get(monthId);
      if (min == null || (currentPosition != null && currentPosition < min) ){
        minPosition.put(monthId, currentPosition);
      }

      Glob firstTransactionInBankMonth = firstTransactionForMonth.get(monthId);
      if ((firstTransactionInBankMonth == null) ||
          (TransactionComparator.ASCENDING_ACCOUNT.compare(transaction, firstTransactionInBankMonth) < 0)) {
        firstTransactionForMonth.put(monthId, transaction);
      }

      Glob lastTransactionInBankMonth = lastTransactionForMonth.get(monthId);
      if ((lastTransactionInBankMonth == null)
          || (TransactionComparator.ASCENDING_ACCOUNT.compare(transaction, lastTransactionInBankMonth) > 0)) {
        lastTransactionForMonth.put(monthId, transaction);
      }

      if (absoluteFirstTransaction == null ||
          (TransactionComparator.ASCENDING_ACCOUNT.compare(transaction, absoluteFirstTransaction) < 0)) {
        absoluteFirstTransaction = transaction;
      }

      if (!transaction.isTrue(Transaction.PLANNED) &&
          (lastRealKnownTransaction == null ||
           TransactionComparator.ASCENDING_ACCOUNT.compare(transaction, lastRealKnownTransaction) > 0)
          && transaction.get(Transaction.POSITION_MONTH).equals(currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH))
          && transaction.get(Transaction.POSITION_DAY) <= currentMonth.get(CurrentMonth.LAST_TRANSACTION_DAY)) {
        lastRealKnownTransaction = transaction;
      }
    }

    private void complete() {
      GlobList months = repository.getAll(Month.TYPE).sort(Month.ID);
      Glob endOfMonthTransaction = null;
      for (Glob month : months) {
        Integer monthId = month.get(Month.ID);

        Double beginOfMonthPosition = null;
        Double endOfMonthPosition = null;

        Glob beginOfMonthTransaction = firstTransactionForMonth.get(monthId);
        if (beginOfMonthTransaction == null) {
          beginOfMonthTransaction = endOfMonthTransaction;
        }
        Glob nextLast = lastTransactionForMonth.get(monthId);
        endOfMonthTransaction = nextLast == null ? beginOfMonthTransaction : nextLast;
        if (endOfMonthTransaction == null) {
          endOfMonthTransaction = absoluteFirstTransaction;
          beginOfMonthTransaction = absoluteFirstTransaction;
//          if (endOfMonthTransaction == null) {
//            GlobList globList = repository.getAll(Account.TYPE,
//                                                  GlobMatchers.fieldEquals(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()));
//            beginOfMonthPosition = 0.;
//            for (Glob glob : globList) {
//              Double value = glob.get(Account.FIRST_POSITION);
//              if (value != null) {
//                beginOfMonthPosition += value;
//              }
//            }
//            endOfMonthPosition = beginOfMonthPosition;
//          }
        }

        if (beginOfMonthTransaction != null && endOfMonthTransaction != null) {
          endOfMonthPosition = endOfMonthTransaction.get(Transaction.SUMMARY_POSITION);
          beginOfMonthPosition = beginOfMonthTransaction.get(Transaction.SUMMARY_POSITION);
          if (beginOfMonthPosition != null) {
            if (beginOfMonthTransaction.get(Transaction.POSITION_MONTH) >= monthId) {
              beginOfMonthPosition = beginOfMonthPosition -
                                     beginOfMonthTransaction.get(Transaction.AMOUNT);
            }
            if (endOfMonthPosition != null) {
              if (endOfMonthTransaction.get(Transaction.POSITION_MONTH) > monthId) {
                endOfMonthPosition = endOfMonthPosition -
                                     endOfMonthTransaction.get(Transaction.AMOUNT);
              }
            }
          }
        }

        Double minPosition = this.minPosition.get(monthId);
        if (minPosition == null && beginOfMonthPosition != null && endOfMonthPosition != null){
          minPosition = Math.min(beginOfMonthPosition, endOfMonthPosition);
        }
        FieldValuesBuilder values =
          FieldValuesBuilder.init()
            .set(BudgetStat.MONTH, monthId)
            .set(BudgetStat.MIN_POSITION, minPosition)
            .set(BudgetStat.BEGIN_OF_MONTH_ACCOUNT_POSITION, beginOfMonthPosition)
            .set(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION, endOfMonthPosition);

        FieldValues budgetAreaValues = getBudgetAreaValues(repository, monthId);
        values.set(budgetAreaValues);
        values.set(BudgetStat.MONTH_BALANCE, getBalance(budgetAreaValues, monthId));
        repository.create(BudgetStat.TYPE, values.toArray());

        if (lastRealKnownTransaction != null) {
          Integer currentMonthId = lastRealKnownTransaction.get(Transaction.POSITION_MONTH);
          if (currentMonthId.equals(monthId)) {
            repository.update(Key.create(BudgetStat.TYPE, currentMonthId),
                              value(BudgetStat.LAST_KNOWN_ACCOUNT_POSITION,
                                    lastRealKnownTransaction.get(Transaction.SUMMARY_POSITION)),
                              value(BudgetStat.LAST_KNOWN_ACCOUNT_POSITION_DAY,
                                    lastRealKnownTransaction.get(Transaction.POSITION_DAY)));
          }
        }
      }
    }

    private double getBalance(FieldValues values, Integer monthId) {
      double balance = 0;
      for (BudgetArea budgetArea : BudgetArea.INCOME_AND_EXPENSES_AREAS) {
        Double amount = values.get(BudgetStat.getSummary(budgetArea));
        if (amount != null) {
          balance += amount;
        }
      }
      Double uncategorized = values.get(BudgetStat.UNCATEGORIZED);
      if (uncategorized != null) {
        balance += uncategorized;
      }
      return balance;
    }

    private MutableFieldValues getBudgetAreaValues(GlobRepository repository, Integer monthId) {

      budgetAreaAmounts.clear();
      Integer lastTransactionMonthId = currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH);
      BudgetAreaAmounts savingsInAmounts = new BudgetAreaAmounts(BudgetArea.SAVINGS);
      BudgetAreaAmounts savingsOutAmounts = new BudgetAreaAmounts(BudgetArea.SAVINGS);

      for (Glob stat : repository.findByIndex(SeriesStat.MONTH_INDEX, monthId)) {
        Glob series = repository.findLinkTarget(stat, SeriesStat.SERIES);
        BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));

        BudgetAreaAmounts amounts = budgetAreaAmounts.get(budgetArea);
        if (amounts == null) {
          amounts = new BudgetAreaAmounts(budgetArea);
          budgetAreaAmounts.put(budgetArea, amounts);
        }

        if (budgetArea.equals(BudgetArea.SAVINGS)) {
          Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
          Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
          if (!(fromAccount != null && AccountType.MAIN.getId().equals(fromAccount.get(Account.ACCOUNT_TYPE))
                || (toAccount != null && AccountType.MAIN.getId().equals(toAccount.get(Account.ACCOUNT_TYPE))))) {
            continue;
          }
          if (series.get(Series.MIRROR_SERIES) != null) {
            if (fromAccount != null
                && AccountType.MAIN.getId().equals(fromAccount.get(Account.ACCOUNT_TYPE))
                && !(Series.isFrom(series, fromAccount))) {
              continue;
            }
            if (toAccount != null
                && AccountType.MAIN.getId().equals(toAccount.get(Account.ACCOUNT_TYPE))
                && !Series.isTo(series, toAccount)) {
              continue;
            }
          }
          if (toAccount != null && AccountType.MAIN.getId().equals(toAccount.get(Account.ACCOUNT_TYPE))) {
            savingsInAmounts.addValues(stat, lastTransactionMonthId);
          }
          else {
            savingsOutAmounts.addValues(stat, lastTransactionMonthId);
          }
          amounts.addValues(stat, lastTransactionMonthId);
        }
        else {
          amounts.addValues(stat, lastTransactionMonthId);
        }
      }

      FieldValuesBuilder values = new FieldValuesBuilder();

      BudgetAreaAmounts uncategorizedAmounts = budgetAreaAmounts.remove(BudgetArea.UNCATEGORIZED);
      if (uncategorizedAmounts != null) {
        values.set(BudgetStat.UNCATEGORIZED, uncategorizedAmounts.getAmount());
      }

      BudgetAreaAmounts expensesAmounts = new BudgetAreaAmounts(BudgetArea.ALL);

      for (Map.Entry<BudgetArea, BudgetAreaAmounts> entry : budgetAreaAmounts.entrySet()) {
        BudgetArea budgetArea = entry.getKey();
        BudgetAreaAmounts amounts = entry.getValue();

        values.set(BudgetStat.getObserved(budgetArea), amounts.getAmount());
        values.set(BudgetStat.getPlanned(budgetArea), amounts.getPlannedAmount());
        values.set(BudgetStat.getPositiveRemaining(budgetArea), amounts.getRemainingPositiveAmount());
        values.set(BudgetStat.getNegativeRemaining(budgetArea), amounts.getRemainingNegativeAmount());
        values.set(BudgetStat.getSummary(budgetArea), amounts.getSummaryAmount());
        values.set(BudgetStat.getPositiveOverrun(budgetArea), amounts.getOverrunPositiveAmount());
        values.set(BudgetStat.getNegativeOverrun(budgetArea), amounts.getOverrunNegativeAmount());

        if (!budgetArea.isIncome()) {
          expensesAmounts.addValues(amounts);
        }
      }

      values.set(BudgetStat.EXPENSE, expensesAmounts.getAmount());
      values.set(BudgetStat.EXPENSE_PLANNED, expensesAmounts.getPlannedAmount());
      values.set(BudgetStat.EXPENSE_REMAINING, expensesAmounts.getRemainingPositiveAmount());
      values.set(BudgetStat.EXPENSE_OVERRUN, expensesAmounts.getOverrunPositiveAmount());
      values.set(BudgetStat.EXPENSE_SUMMARY, expensesAmounts.getSummaryAmount());

      values.set(BudgetStat.SAVINGS_IN, savingsInAmounts.getAmount());
      values.set(BudgetStat.SAVINGS_IN_PLANNED, savingsInAmounts.getPlannedAmount());
      values.set(BudgetStat.SAVINGS_IN_POSITIVE_REMAINING, savingsInAmounts.getRemainingPositiveAmount());
      values.set(BudgetStat.SAVINGS_IN_POSITIVE_OVERRUN, savingsInAmounts.getOverrunPositiveAmount());
      values.set(BudgetStat.SAVINGS_IN_NEGATIVE_REMAINING, savingsInAmounts.getRemainingNegativeAmount());
      values.set(BudgetStat.SAVINGS_IN_NEGATIVE_OVERRUN, savingsInAmounts.getOverrunNegativeAmount());
      values.set(BudgetStat.SAVINGS_IN_SUMMARY, savingsInAmounts.getSummaryAmount());

      values.set(BudgetStat.SAVINGS_IN, savingsOutAmounts.getAmount());
      values.set(BudgetStat.SAVINGS_OUT_PLANNED, savingsOutAmounts.getPlannedAmount());
      values.set(BudgetStat.SAVINGS_OUT_POSITIVE_REMAINING, savingsOutAmounts.getRemainingPositiveAmount());
      values.set(BudgetStat.SAVINGS_OUT_POSITIVE_OVERRUN, savingsOutAmounts.getOverrunPositiveAmount());
      values.set(BudgetStat.SAVINGS_OUT_NEGATIVE_OVERRUN, savingsOutAmounts.getOverrunNegativeAmount());
      values.set(BudgetStat.SAVINGS_OUT_NEGATIVE_REMAINING, savingsOutAmounts.getRemainingNegativeAmount());
      values.set(BudgetStat.SAVINGS_OUT_SUMMARY, savingsOutAmounts.getSummaryAmount());

      double uncategorizedAbs = 0;
      for (Glob transaction : Transaction.getUncategorizedTransactions(monthId, repository)) {
        uncategorizedAbs += Math.abs(transaction.get(Transaction.AMOUNT));
      }
      values.set(BudgetStat.UNCATEGORIZED_ABS, uncategorizedAbs);

      return values.get();
    }

  }

  private class BudgetAreaAmounts {
    private BudgetArea budgetArea;
    private double amount;
    private double plannedAmount;
    private double remainingPositiveAmount;
    private double summaryAmount;
    private double overrunPositiveAmount;
    private double overrunNegativeAmount;
    private double remainingNegativeAmount;

    public BudgetAreaAmounts(BudgetArea budgetArea) {
      this.budgetArea = budgetArea;
    }

    public void addValues(Glob seriesStat, int currentMonthId) {
      double seriesAmount = Utils.zeroIfNull(seriesStat.get(SeriesStat.ACTUAL_AMOUNT));
      double seriesPlannedAmount = Utils.zeroIfNull(seriesStat.get(SeriesStat.PLANNED_AMOUNT));
      double seriesRemainingAmount = Utils.zeroIfNull(seriesStat.get(SeriesStat.REMAINING_AMOUNT));
      double serisOverrunAmount = Utils.zeroIfNull(seriesStat.get(SeriesStat.OVERRUN_AMOUNT));

      amount += seriesAmount;
      plannedAmount += seriesPlannedAmount;
      if (seriesRemainingAmount >0){
        remainingPositiveAmount += seriesRemainingAmount;
      }
      else {
        remainingNegativeAmount += seriesRemainingAmount;
      }
      if (serisOverrunAmount > 0){
        overrunPositiveAmount += serisOverrunAmount;
      }
      else {
        overrunNegativeAmount += serisOverrunAmount;
      }

      int monthId = seriesStat.get(SeriesStat.MONTH);
      if (monthId < currentMonthId) {
        summaryAmount += seriesAmount;
      }
      else {
        summaryAmount += Amounts.max(seriesAmount, seriesPlannedAmount, budgetArea.isIncome());
      }
    }

    public void addValues(BudgetAreaAmounts amounts) {
      this.amount += amounts.amount;
      this.summaryAmount += amounts.summaryAmount;
      this.plannedAmount += amounts.plannedAmount;
      this.remainingPositiveAmount += amounts.remainingPositiveAmount;
      this.remainingNegativeAmount += amounts.remainingNegativeAmount;
      this.overrunPositiveAmount += amounts.overrunPositiveAmount;
      this.overrunNegativeAmount += amounts.overrunNegativeAmount;
    }

    public Double getAmount() {
      return amount;
    }

    public double getPlannedAmount() {
      return plannedAmount;
    }

    public double getRemainingPositiveAmount() {
      return remainingPositiveAmount;
    }

    public double getOverrunNegativeAmount() {
      return overrunNegativeAmount;
    }

    public double getRemainingNegativeAmount() {
      return remainingNegativeAmount;
    }

    public Double getSummaryAmount() {
      return summaryAmount;
    }

    public double getOverrunPositiveAmount() {
      return overrunPositiveAmount;
    }
  }
}
