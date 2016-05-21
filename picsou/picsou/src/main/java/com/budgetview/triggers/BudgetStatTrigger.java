package com.budgetview.triggers;

import com.budgetview.gui.categorization.utils.Uncategorized;
import com.budgetview.gui.model.SeriesStat;
import com.budgetview.model.*;
import com.budgetview.shared.utils.Amounts;
import com.budgetview.utils.TransactionComparator;
import com.budgetview.gui.model.AccountStat;
import com.budgetview.gui.model.BudgetStat;
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
import static org.globsframework.model.utils.GlobMatchers.*;


/*
 TODO: updater ce trigger en sachant qu'il y a une operation de debut et une de fin + verifier que
 TODO: absoluteFirstTransaction a un sens (cf Account.FIRST_POSITION)
 */

public class BudgetStatTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE) || changeSet.containsChanges(SeriesBudget.TYPE) ||
        changeSet.containsUpdates(Series.BUDGET_AREA) || changeSet.containsChanges(CurrentMonth.TYPE) ||
        changeSet.containsChanges(Account.TYPE)) {
      computeStat(repository);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Transaction.TYPE) || changedTypes.contains(SeriesBudget.TYPE) ||
        changedTypes.contains(Series.TYPE) || changedTypes.contains(CurrentMonth.TYPE) ||
        changedTypes.contains(Account.TYPE)) {
      computeStat(repository);
    }
  }

  private void computeStat(GlobRepository repository) {
    repository.startChangeSet();
    try {
      repository.deleteAll(BudgetStat.TYPE);
      repository.deleteAll(AccountStat.TYPE);
      AccountStatBuilder mainAccountStatBuilder = new AccountStatBuilder(repository);
      if (mainAccountStatBuilder.currentMonth == null) {
        return;
      }
      computeStat(repository, Account.MAIN_SUMMARY_ACCOUNT_ID, AccountType.MAIN);
      computeStat(repository, Account.SAVINGS_SUMMARY_ACCOUNT_ID, AccountType.SAVINGS);
      mainAccountStatBuilder.complete();
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private void computeStat(GlobRepository repository, int summaryAccountId, AccountType accountType) {
    BudgetStatComputer budgetStatComputer = new BudgetStatComputer(summaryAccountId, repository);
    Set<Integer> accountIds =
      repository.getAll(Account.TYPE,
                        and(fieldEquals(Account.ACCOUNT_TYPE, accountType.getId()),
                            not(GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()))))
        .getValueSet(Account.ID);
    Glob[] transactions = Transaction
      .getAllSortedByPositionDate(repository,
                                  GlobMatchers.contained(Transaction.ACCOUNT, accountIds),
                                  TransactionComparator.ASCENDING_ACCOUNT);

    for (Glob transaction : transactions) {
      budgetStatComputer.run(transaction);
    }
    if (transactions.length != 0) {
      budgetStatComputer.minSummaryPosition.newMonth(transactions[transactions.length - 1].get(Transaction.POSITION_MONTH));
    }

    Integer[] months = repository.getAll(Month.TYPE).getSortedArray(Month.ID);
    for (Integer accountId : accountIds) {
      Glob last = null;
      for (Integer month : months) {
        Glob current = repository.find(Key.create(AccountStat.ACCOUNT, accountId, AccountStat.MONTH, month));
        if ((current == null) && (last != null)) {
          Double lastEnd = last.get(AccountStat.END_POSITION);
          repository.create(AccountStat.TYPE,
                            value(AccountStat.MONTH, month),
                            value(AccountStat.ACCOUNT, accountId),
                            value(AccountStat.BEGIN_POSITION, lastEnd),
                            value(AccountStat.MIN_POSITION, lastEnd),
                            value(AccountStat.FUTURE_MIN_POSITION, lastEnd),
                            value(AccountStat.END_POSITION, lastEnd),
                            value(AccountStat.ACCOUNT_COUNT, accountIds.size()),
                            value(AccountStat.SUMMARY_POSITION_AT_MIN, last.get(AccountStat.SUMMARY_POSITION_AT_MIN)));
        }
        last = current;
      }
    }
  }

  private class BudgetStatComputer {
    private int month;
    private MinSummaryPosition minSummaryPosition;

    private BudgetStatComputer(final int summaryAccountId, GlobRepository repository) {
      minSummaryPosition = new MinSummaryPosition(summaryAccountId, repository);
    }

    public void run(Glob transaction) {
      if (transaction.get(Transaction.SUMMARY_POSITION) == null) {
        Log.write("Summary position is null for transaction : " + transaction.get(Transaction.ID) +
                  " " + transaction.get(Transaction.LABEL));
      }

      Integer monthId = transaction.get(Transaction.POSITION_MONTH);
      if (month != 0 && month != monthId) {
        while (month < monthId) {
          minSummaryPosition.newMonth(month);
          month = Month.next(month);
        }
      }
      month = monthId;

      Double currentAccountPosition = transaction.get(Transaction.ACCOUNT_POSITION);
      Double total = transaction.get(Transaction.SUMMARY_POSITION);
      if (currentAccountPosition != null && total != null) {
        minSummaryPosition.addTransaction(transaction.get(Transaction.ACCOUNT),
                                          currentAccountPosition,
                                          total, transaction.get(Transaction.PLANNED),
                                          transaction.get(Transaction.TRANSACTION_TYPE) == TransactionType.CLOSE_ACCOUNT_EVENT.getId());
      }
    }
  }

  private static class AccountStatBuilder {
    private Map<BudgetArea, BudgetAreaAmounts> budgetAreaAmounts = new HashMap<BudgetArea, BudgetAreaAmounts>();
    private GlobRepository repository;
    private Glob currentMonth;

    AccountStatBuilder(GlobRepository repository) {
      this.repository = repository;
      currentMonth = repository.find(CurrentMonth.KEY);
    }

    private void complete() {
      GlobList months = repository.getAll(Month.TYPE).sort(Month.ID);
      for (Glob month : months) {
        Integer monthId = month.get(Month.ID);
        FieldValuesBuilder values =
          FieldValuesBuilder.init()
            .set(BudgetStat.MONTH, monthId);

        FieldValues budgetAreaValues = getBudgetAreaValues(repository, monthId);
        values.set(budgetAreaValues);
        values.set(BudgetStat.MONTH_BALANCE, getBalance(budgetAreaValues, monthId));
        repository.create(BudgetStat.TYPE, values.toArray());

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
      BudgetAreaAmounts savingsInAmounts = new BudgetAreaAmounts(BudgetArea.TRANSFER);
      BudgetAreaAmounts savingsOutAmounts = new BudgetAreaAmounts(BudgetArea.TRANSFER);

      for (Glob stat : SeriesStat.getAllSummarySeriesForMonth(monthId, repository)) {
        Glob series = SeriesStat.getSeries(stat, repository);
        BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));

        BudgetAreaAmounts amounts = budgetAreaAmounts.get(budgetArea);
        if (amounts == null) {
          amounts = new BudgetAreaAmounts(budgetArea);
          budgetAreaAmounts.put(budgetArea, amounts);
        }

        if (budgetArea.equals(BudgetArea.TRANSFER)) {
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

      values.set(BudgetStat.UNCATEGORIZED_ABS, Uncategorized.getAbsAmount(monthId, repository));

      return values.get();
    }
  }

  private static class BudgetAreaAmounts {
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
      double seriesOverrunAmount = Utils.zeroIfNull(seriesStat.get(SeriesStat.OVERRUN_AMOUNT));

      amount += seriesAmount;
      plannedAmount += seriesPlannedAmount;
      if (seriesRemainingAmount > 0) {
        remainingPositiveAmount += seriesRemainingAmount;
      }
      else {
        remainingNegativeAmount += seriesRemainingAmount;
      }
      if (seriesOverrunAmount > 0) {
        overrunPositiveAmount += seriesOverrunAmount;
      }
      else {
        overrunNegativeAmount += seriesOverrunAmount;
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

  private static class MinSummaryPosition {
    private int summaryAccountId;
    private final GlobRepository repository;
    private MinAccountPosition minAccountPosition = null;
    private Map<Integer, MinAccountPosition> minAccountPositions = new HashMap<Integer, MinAccountPosition>();

    MinSummaryPosition(final int summaryAccountId, GlobRepository repository) {
      this.repository = repository;
      this.summaryAccountId = summaryAccountId;
    }

    void addTransaction(int accountId, double currentAccountPosition, double total, boolean isFuture, boolean isClosed) {
      MinAccountPosition position = minAccountPositions.get(accountId);
      if (position == null) {
        position = new MinAccountPosition(accountId, currentAccountPosition, total, isFuture, isClosed);
        minAccountPositions.put(accountId, position);
      }
      else {
        position.push(accountId, currentAccountPosition, total, isFuture, isClosed);
      }
      // on inverse total et current pour que le critere de min soit sur le total et qu'on sauve en meme
      // temps le current et on reinverse dans le newMonth
      if (minAccountPosition == null) {
        minAccountPosition = new MinAccountPosition(accountId, total, currentAccountPosition, false, false);
      }
      else {
        minAccountPosition.push(accountId, total, currentAccountPosition, false, false);
      }
    }

    void newMonth(int month) {
      if (minAccountPositions.size() == 0) {
        return;
      }
      int currentMinAccount = -1;
      double total = 0;
      double min = Double.POSITIVE_INFINITY;
      MinAccountPosition minAccount = null;

      for (Map.Entry<Integer, MinAccountPosition> entry : minAccountPositions.entrySet()) {
        MinAccountPosition currentAccount = entry.getValue();
        repository.create(AccountStat.TYPE,
                          value(AccountStat.MONTH, month),
                          value(AccountStat.ACCOUNT, currentAccount.account),
                          value(AccountStat.BEGIN_POSITION, currentAccount.begin),
                          value(AccountStat.MIN_POSITION, currentAccount.min),
                          value(AccountStat.FUTURE_MIN_POSITION, currentAccount.futureMin),
                          value(AccountStat.END_POSITION, currentAccount.end),
                          value(AccountStat.ACCOUNT_COUNT, minAccountPositions.size()),
                          value(AccountStat.SUMMARY_POSITION_AT_MIN, currentAccount.total));
        double minToUse;
        double totalToUse;
        if (Double.isNaN(currentAccount.min)) {
          minToUse = currentAccount.futureMin;
          totalToUse = currentAccount.futureTotal;
        }
        else {
          if (Double.isNaN(currentAccount.futureMin)) {
            minToUse = currentAccount.min;
            totalToUse = currentAccount.total;
          }
          else {
            if (currentAccount.min <= currentAccount.futureMin) {
              minToUse = currentAccount.min;
              totalToUse = currentAccount.total;
            }
            else {
              minToUse = currentAccount.futureMin;
              totalToUse = currentAccount.futureTotal;
            }
          }
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
        repository.create(AccountStat.TYPE,
                          value(AccountStat.ACCOUNT, summaryAccountId),
                          value(AccountStat.MONTH, month),
                          value(AccountStat.MIN_ACCOUNT, currentMinAccount),
                          value(AccountStat.MIN_POSITION, min),
                          value(AccountStat.BEGIN_POSITION, minAccountPosition.begin),
                          value(AccountStat.END_POSITION, minAccountPosition.end),
                          value(AccountStat.ACCOUNT_COUNT, minAccountPositions.size()),
                          value(AccountStat.SUMMARY_POSITION_AT_MIN, total));
      }
      for (Iterator<Map.Entry<Integer, MinAccountPosition>> iterator = minAccountPositions.entrySet().iterator(); iterator.hasNext(); ) {
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

    private static class MinAccountPosition {
      int account;
      double begin;
      double end;
      double min = Double.NaN;
      double futureMin = Double.NaN;
      double total = Double.NaN;
      double futureTotal = Double.NaN;
      boolean hasOp;
      boolean closed;

      MinAccountPosition(int account, double value, double total, boolean isFuture, boolean closed) {
        this.account = account;
        this.begin = 0.;
        if (isFuture) {
          this.futureMin = value;
          this.futureTotal = total;
        }
        else {
          this.min = value;
          this.total = total;
        }
        if (closed) {
          end = value;
        }
      }

      void reset() {
        begin = end;
        hasOp = false;
        this.min = Double.NaN;
        this.futureMin = Double.NaN;
        this.total = Double.NaN;
        this.futureTotal = Double.NaN;
      }

      public void push(int account, double current, double total, boolean isFuture, boolean isClosed) {
        this.account = account;
        closed = isClosed;
        hasOp = true;
        if (isFuture) {
          if (Double.isNaN(this.futureMin) || current < this.futureMin) {
            this.futureMin = current;
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
  }
}
