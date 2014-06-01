package org.designup.picsou.triggers;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.MainAccountStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;


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
      repository.deleteAll(MainAccountStat.TYPE);
      BudgetStatComputer budgetStatComputer = new BudgetStatComputer(repository);
      if (budgetStatComputer.currentMonth == null) {
        return;
      }
      Set<Integer> wantedAccount = repository.getAll(Account.TYPE, GlobMatchers.fieldEquals(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()))
        .getValueSet(Account.ID);
      Glob[] transactions = Transaction
        .getSortedByPositionDateTransactions(repository,
                                             GlobMatchers.contained(Transaction.ACCOUNT, wantedAccount),
                                             TransactionComparator.ASCENDING_ACCOUNT);

      for (Glob transaction : transactions) {
        budgetStatComputer.run(transaction);
      }
      if (transactions.length != 0){
        budgetStatComputer.minPosition.newMonth(transactions[transactions.length - 1].get(Transaction.POSITION_MONTH));
      }
      budgetStatComputer.complete();
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private class BudgetStatComputer {
    private Map<BudgetArea, BudgetAreaAmounts> budgetAreaAmounts = new HashMap<BudgetArea, BudgetAreaAmounts>();
    private int month;
    private Glob currentMonth;
    private MinPosition minPosition;

    private GlobRepository repository;

    private BudgetStatComputer(GlobRepository repository) {
      this.repository = repository;
      currentMonth = repository.find(CurrentMonth.KEY);
      minPosition = new MinPosition(this.repository);
    }

    public void run(Glob transaction) {
      if (transaction.get(Transaction.SUMMARY_POSITION) == null) {
        Log.write("Summary position is null for transaction : " + transaction.get(Transaction.ID) +
                  " " + transaction.get(Transaction.LABEL));
      }

      Integer monthId = transaction.get(Transaction.POSITION_MONTH);
      if (monthId < month){
        System.out.println("BudgetStatComputer.run");
      }
      if (month != 0 && month != monthId) {
        while (month < monthId) {
          minPosition.newMonth(month++);
        }
      }
      month = monthId;

      Double currentPosition = transaction.get(Transaction.ACCOUNT_POSITION);
      Double total = transaction.get(Transaction.SUMMARY_POSITION);
      if (currentPosition != null && total != null) {
        minPosition.add(currentPosition, transaction.get(Transaction.ACCOUNT),
                        total, transaction.get(Transaction.PLANNED),
                        transaction.get(Transaction.TRANSACTION_TYPE) == TransactionType.CLOSE_ACCOUNT_EVENT.getId());
      }

//      Glob firstTransactionInBankMonth = firstTransactionForMonth.get(monthId);
//      if ((firstTransactionInBankMonth == null) ||
//          (TransactionComparator.ASCENDING_ACCOUNT.compare(transaction, firstTransactionInBankMonth) < 0)) {
//        firstTransactionForMonth.put(monthId, transaction);
//      }
//
//      Glob lastTransactionInBankMonth = lastTransactionForMonth.get(monthId);
//      if ((lastTransactionInBankMonth == null)
//          || (TransactionComparator.ASCENDING_ACCOUNT.compare(transaction, lastTransactionInBankMonth) > 0)) {
//        lastTransactionForMonth.put(monthId, transaction);
//      }
//
//      if (absoluteFirstTransaction == null ||
//          (TransactionComparator.ASCENDING_ACCOUNT.compare(transaction, absoluteFirstTransaction) < 0)) {
//        absoluteFirstTransaction = transaction;
//      }
//
//      if (!transaction.isTrue(Transaction.PLANNED) &&
//          (lastRealKnownTransaction == null ||
//           TransactionComparator.ASCENDING_ACCOUNT.compare(transaction, lastRealKnownTransaction) > 0)
//          && transaction.get(Transaction.POSITION_MONTH).equals(currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH))
//          && transaction.get(Transaction.POSITION_DAY) <= currentMonth.get(CurrentMonth.LAST_TRANSACTION_DAY)) {
//        lastRealKnownTransaction = transaction;
//      }
    }

    private void complete() {
      GlobList months = repository.getAll(Month.TYPE).sort(Month.ID);
//      Glob endOfMonthTransaction = null;
      for (Glob month : months) {
        Integer monthId = month.get(Month.ID);

//        Double beginOfMonthPosition = null;
//        Double endOfMonthPosition = null;
//
//        Glob beginOfMonthTransaction = firstTransactionForMonth.get(monthId);
//        if (beginOfMonthTransaction == null) {
//          beginOfMonthTransaction = endOfMonthTransaction;
//        }
//        Glob nextLast = lastTransactionForMonth.get(monthId);
//        endOfMonthTransaction = nextLast == null ? beginOfMonthTransaction : nextLast;
//        if (endOfMonthTransaction == null) {
//          endOfMonthTransaction = absoluteFirstTransaction;
//          beginOfMonthTransaction = absoluteFirstTransaction;
////          if (endOfMonthTransaction == null) {
////            GlobList globList = repository.getAll(Account.TYPE,
////                                                  GlobMatchers.fieldEquals(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()));
////            beginOfMonthPosition = 0.;
////            for (Glob glob : globList) {
////              Double value = glob.get(Account.FIRST_POSITION);
////              if (value != null) {
////                beginOfMonthPosition += value;
////              }
////            }
////            endOfMonthPosition = beginOfMonthPosition;
////          }
//        }

//        if (beginOfMonthTransaction != null && endOfMonthTransaction != null) {
//          endOfMonthPosition = endOfMonthTransaction.get(Transaction.SUMMARY_POSITION);
//          beginOfMonthPosition = beginOfMonthTransaction.get(Transaction.SUMMARY_POSITION);
//          if (beginOfMonthPosition != null) {
//            if (beginOfMonthTransaction.get(Transaction.POSITION_MONTH) >= monthId) {
//              beginOfMonthPosition = beginOfMonthPosition -
//                                     beginOfMonthTransaction.get(Transaction.AMOUNT);
//            }
//            if (endOfMonthPosition != null) {
//              if (endOfMonthTransaction.get(Transaction.POSITION_MONTH) > monthId) {
//                endOfMonthPosition = endOfMonthPosition -
//                                     endOfMonthTransaction.get(Transaction.AMOUNT);
//              }
//            }
//          }
//        }

//        MinAccountPosition minPosition = this.minPosition.get(monthId);
//        if (minPosition != null) {
//          repository.create(MainAccountStat.TYPE,
//                            value(MainAccountStat.MONTH, monthId),
//                            value(MainAccountStat.ACCOUNT_WITH_MIN, minPosition.account.get(Account.ID)),
//                            value(MainAccountStat.MIN_POSITION, minPosition.min),
//                            value(MainAccountStat.SUMMARY_POSITION_AT_MIN, minPosition.total));
//        }
        FieldValuesBuilder values =
          FieldValuesBuilder.init()
            .set(BudgetStat.MONTH, monthId);
//            .set(BudgetStat.BEGIN_OF_MONTH_ACCOUNT_POSITION, beginOfMonthPosition)
//            .set(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION, endOfMonthPosition);

        FieldValues budgetAreaValues = getBudgetAreaValues(repository, monthId);
        values.set(budgetAreaValues);
        values.set(BudgetStat.MONTH_BALANCE, getBalance(budgetAreaValues, monthId));
        repository.create(BudgetStat.TYPE, values.toArray());

//        if (lastRealKnownTransaction != null) {
//          Integer currentMonthId = lastRealKnownTransaction.get(Transaction.POSITION_MONTH);
//          if (currentMonthId.equals(monthId)) {
//            repository.update(Key.create(BudgetStat.TYPE, currentMonthId),
//                              value(BudgetStat.LAST_KNOWN_ACCOUNT_POSITION,
//                                    lastRealKnownTransaction.get(Transaction.SUMMARY_POSITION)),
//                              value(BudgetStat.LAST_KNOWN_ACCOUNT_POSITION_DAY,
//                                    lastRealKnownTransaction.get(Transaction.POSITION_DAY)));
//          }
//        }
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

      for (Glob stat : SeriesStat.getAllSeriesForMonth(monthId, repository)) {
        Glob series = SeriesStat.getSeries(stat, repository);
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
      if (seriesRemainingAmount > 0) {
        remainingPositiveAmount += seriesRemainingAmount;
      }
      else {
        remainingNegativeAmount += seriesRemainingAmount;
      }
      if (serisOverrunAmount > 0) {
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

  static class MinAccountPosition {
    double begin;
    double end;
    private boolean isFuture;
    double min = Double.NaN;
    double minFuture = Double.NaN;
    double total = Double.NaN;
    double futureTotal = Double.NaN;
    int account;
    boolean hasOp;
    boolean closed;

    MinAccountPosition(int account, double value, boolean isFuture, boolean closed) {
      this.account = account;
      this.begin = 0.;
      if (isFuture){
        minFuture =  value;
      }
      else {
        min = value;
      }
      if (closed){
        end = value;
      }
    }

    void reset() {
      begin = end;
      hasOp = false;
      this.min = Double.NaN;
      this.minFuture = Double.NaN;
      this.total = Double.NaN;
      this.futureTotal = Double.NaN;
    }


    public void push(int account, double current, double total, boolean isFuture, boolean isClosed) {
      this.account = account;
      closed = isClosed;
      this.isFuture = isFuture;
      hasOp = true;
      if (isFuture) {
        if (Double.isNaN(this.minFuture) || current < this.minFuture) {
          this.minFuture = current;
          this.futureTotal = total;
        }
      }
      else {
        if (Double.isNaN(this.min) || current < this.min) {
          this.min = current;
          this.total = total;
        }
      }
      this.end = current;
    }
  }

  static class MinPosition {
    final GlobRepository repository;
    MinAccountPosition minAccountPosition = null;
    Map<Integer, MinAccountPosition> accountToMin = new HashMap<Integer, MinAccountPosition>();

    MinPosition(GlobRepository repository) {
      this.repository = repository;
    }

    void newMonth(int month) {
      if (accountToMin.size() == 0) {
        return;
      }
      int currentMinAccount = -1;
      double total = 0;
      double min = Double.POSITIVE_INFINITY;
      MinAccountPosition minAccount = null;

      for (Map.Entry<Integer, MinAccountPosition> entry : accountToMin.entrySet()) {
        MinAccountPosition currentAccount = entry.getValue();
        repository.create(MainAccountStat.TYPE,
                          value(MainAccountStat.MONTH, month),
                          value(MainAccountStat.ACCOUNT, currentAccount.account),
                          value(MainAccountStat.BEGIN_POSITION, currentAccount.begin),
                          value(MainAccountStat.MIN_POSITION, currentAccount.min),
                          value(MainAccountStat.FUTURE_MIN_POSITION, currentAccount.minFuture),
                          value(MainAccountStat.END_POSITION, currentAccount.end),
                          value(MainAccountStat.SUMMARY_POSITION_AT_FUTURE_MIN, currentAccount.futureTotal),
                          value(MainAccountStat.SUMMARY_POSITION_AT_MIN, currentAccount.total));
        double minToUse;
        double totalToUse;
        if (!Double.isNaN(currentAccount.min)) {
          if (!Double.isNaN(currentAccount.minFuture)) {
            if (currentAccount.min <= currentAccount.minFuture) {
              minToUse = currentAccount.min;
              totalToUse = currentAccount.total;
            }
            else {
              minToUse = currentAccount.minFuture;
              totalToUse = currentAccount.futureTotal;
            }
          }
          else {
            minToUse = currentAccount.min;
            totalToUse = currentAccount.total;
          }
        }
        else {
          minToUse = currentAccount.minFuture;
          totalToUse = currentAccount.futureTotal;
        }
        if (minToUse < min) {
          minAccount = currentAccount;
          min = minToUse;
          total = totalToUse;
          currentMinAccount = entry.getKey();
        }
      }
      if (minAccount == null || !minAccount.hasOp) {
        min = minAccount == null ? minAccountPosition.total : min;
        total = minAccountPosition.min;
        currentMinAccount = minAccount == null ? minAccountPosition.account : currentMinAccount;
      }
      if (min != Double.POSITIVE_INFINITY) {
        repository.create(MainAccountStat.TYPE,
                          value(MainAccountStat.ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID),
                          value(MainAccountStat.MONTH, month),
                          value(MainAccountStat.MIN_ACCOUNT, currentMinAccount),
                          value(MainAccountStat.MIN_POSITION, min),
                          value(MainAccountStat.BEGIN_POSITION, minAccountPosition.begin),
                          value(MainAccountStat.END_POSITION, minAccountPosition.end),
                          value(MainAccountStat.ACCOUNT_COUNT, accountToMin.size()),
                          value(MainAccountStat.SUMMARY_POSITION_AT_MIN, total));
      }
      for (Iterator<Map.Entry<Integer, MinAccountPosition>> iterator = accountToMin.entrySet().iterator(); iterator.hasNext(); ) {
        Map.Entry<Integer, MinAccountPosition> next = iterator.next();
        if (next.getValue().closed) {
          iterator.remove();
        }
        else {
          next.getValue().reset();
        }
      }
      minAccountPosition.reset();
    }

    void add(double current, int accountId, double total, boolean isFuture, boolean isClosed) {
      MinAccountPosition position = accountToMin.get(accountId);
      if (position == null) {
        position = new MinAccountPosition(accountId, current, isFuture, isClosed);
        accountToMin.put(accountId, position);
      }
      else {
        position.push(accountId, current, total, isFuture, isClosed);
      }
      // on inverse total et current pour que le critere de min soit sur le total et qu'on sauve en meme
      // temps le current et on reinverse dans le newMonth
      if (minAccountPosition == null){
        minAccountPosition = new MinAccountPosition(accountId, total, false, false);
      } else {
        minAccountPosition.push(accountId, total, current, false, false);
      }
    }

    public MinAccountPosition get(Integer monthId) {
      return accountToMin.get(monthId);
    }

  }

}
