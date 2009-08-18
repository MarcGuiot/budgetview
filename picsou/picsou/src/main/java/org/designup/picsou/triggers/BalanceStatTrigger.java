package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BalanceStatTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE) || changeSet.containsChanges(SeriesBudget.TYPE)) {
      computeStat(repository);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    computeStat(repository);
  }

  private void computeStat(GlobRepository repository) {
    repository.startChangeSet();
    try {
      repository.deleteAll(BalanceStat.TYPE);
      BalanceStatCalculator balanceStatCalculator = new BalanceStatCalculator(repository);
      repository.safeApply(Transaction.TYPE, GlobMatchers.ALL, balanceStatCalculator);
      balanceStatCalculator.complete();
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private class BalanceStatCalculator implements GlobFunctor {
    private Map<Integer, Glob> firstTransactionForMonth = new HashMap<Integer, Glob>();
    private Map<Integer, Glob> lastTransactionForMonth = new HashMap<Integer, Glob>();
    private Map<BudgetArea, BudgetAreaAmounts> budgetAreaAmounts = new HashMap<BudgetArea, BudgetAreaAmounts>();
    private Glob lastRealKnownTransaction;
    private Glob currentMonth;
    private Glob absoluteFirstTransaction;

    private GlobRepository repository;

    private BalanceStatCalculator(GlobRepository repository) {
      this.repository = repository;
      this.currentMonth = repository.get(CurrentMonth.KEY);
    }

    public void run(Glob transaction, GlobRepository repository) throws Exception {
      if (transaction.get(Transaction.SUMMARY_POSITION) == null) {
        Log.write("Summary position is null for transaction : " + transaction.get(Transaction.ID) +
                  " " + transaction.get(Transaction.LABEL));
      }

      Glob account = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
      if (!account.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())) {
        return;
      }

      Integer monthId = transaction.get(Transaction.BANK_MONTH);
      Glob firstTransactionInBankMonth = firstTransactionForMonth.get(monthId);
      if ((firstTransactionInBankMonth == null) ||
          (TransactionComparator.ASCENDING_BANK.compare(transaction, firstTransactionInBankMonth) < 0)) {
        firstTransactionForMonth.put(monthId, transaction);
      }

      Glob lastTransactionInBankMonth = lastTransactionForMonth.get(monthId);
      if ((lastTransactionInBankMonth == null)
          || (TransactionComparator.ASCENDING_BANK.compare(transaction, lastTransactionInBankMonth) > 0)) {
        lastTransactionForMonth.put(monthId, transaction);
      }

      if (absoluteFirstTransaction == null ||
          (TransactionComparator.ASCENDING_BANK.compare(transaction, absoluteFirstTransaction) < 0)) {
        absoluteFirstTransaction = transaction;
      }

      if (!transaction.get(Transaction.PLANNED) &&
          (lastRealKnownTransaction == null ||
           TransactionComparator.ASCENDING_BANK.compare(transaction, lastRealKnownTransaction) > 0)
          && transaction.get(Transaction.BANK_MONTH).equals(currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH))
          && transaction.get(Transaction.BANK_DAY) <= currentMonth.get(CurrentMonth.LAST_TRANSACTION_DAY)) {
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
          if (endOfMonthTransaction == null) {
            GlobList globList = repository.getAll(Account.TYPE,
                                                  GlobMatchers.fieldEquals(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()));
            beginOfMonthPosition = 0.;
            for (Glob glob : globList) {
              Double value = glob.get(Account.FIRST_POSITION);
              if (value != null) {
                beginOfMonthPosition += value;
              }
            }
            endOfMonthPosition = beginOfMonthPosition;
          }
        }

        if (beginOfMonthTransaction != null && endOfMonthTransaction != null) {
          endOfMonthPosition = endOfMonthTransaction.get(Transaction.SUMMARY_POSITION);
          beginOfMonthPosition = beginOfMonthTransaction.get(Transaction.SUMMARY_POSITION);
          if (beginOfMonthPosition != null) {
            if (beginOfMonthTransaction.get(Transaction.BANK_MONTH) >= monthId) {
              beginOfMonthPosition = beginOfMonthPosition -
                                     beginOfMonthTransaction.get(Transaction.AMOUNT);
            }
            if (endOfMonthPosition != null) {
              if (endOfMonthTransaction.get(Transaction.BANK_MONTH) > monthId) {
                endOfMonthPosition = endOfMonthPosition -
                                     endOfMonthTransaction.get(Transaction.AMOUNT);
              }
            }
          }
        }

        FieldValuesBuilder values =
          FieldValuesBuilder.init()
            .set(BalanceStat.MONTH, monthId)
            .set(BalanceStat.BEGIN_OF_MONTH_ACCOUNT_POSITION, beginOfMonthPosition)
            .set(BalanceStat.END_OF_MONTH_ACCOUNT_POSITION, endOfMonthPosition);

        FieldValues budgetAreaValues = getBudgetAreaValues(repository, monthId);
        values.set(budgetAreaValues);
        values.set(BalanceStat.MONTH_BALANCE, getBalance(budgetAreaValues, monthId));
        Glob stat = repository.create(BalanceStat.TYPE, values.toArray());

        if (lastRealKnownTransaction != null) {
          Integer currentMonthId = lastRealKnownTransaction.get(Transaction.BANK_MONTH);
          if (currentMonthId.equals(monthId)) {
            repository.update(Key.create(BalanceStat.TYPE, currentMonthId),
                              value(BalanceStat.LAST_KNOWN_ACCOUNT_POSITION,
                                    lastRealKnownTransaction.get(Transaction.SUMMARY_POSITION)),
                              value(BalanceStat.LAST_KNOWN_ACCOUNT_POSITION_DAY,
                                    lastRealKnownTransaction.get(Transaction.BANK_DAY)));
          }
        }
      }
    }

    private double getBalance(FieldValues values, Integer monthId) {
      double balance = 0;
      for (BudgetArea budgetArea : BudgetArea.INCOME_AND_EXPENSES_AREAS) {
        Double amount = values.get(BalanceStat.getSummary(budgetArea));
        if (amount != null) {
          balance += amount;
        }
      }
      Double uncategorized = values.get(BalanceStat.UNCATEGORIZED);
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

      for (Glob stat : repository.getAll(SeriesStat.TYPE, GlobMatchers.fieldEquals(SeriesStat.MONTH, monthId))) {
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
          if (!(fromAccount != null && fromAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())
                || (toAccount != null && toAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())))) {
            continue;
          }
          if (series.get(Series.MIRROR_SERIES) != null) {
            if (fromAccount != null
                && fromAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())
                && !series.get(Series.IS_MIRROR)) {
              continue;
            }
            if (toAccount != null
                && toAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())
                && series.get(Series.IS_MIRROR)) {
              continue;
            }
          }
          if (stat.get(SeriesStat.AMOUNT) >= 0) {
            savingsInAmounts.addValues(stat, lastTransactionMonthId);
          }
          else {
            savingsOutAmounts.addValues(stat, lastTransactionMonthId);
          }
        }

        amounts.addValues(stat, lastTransactionMonthId);
      }

      FieldValuesBuilder values = new FieldValuesBuilder();

      BudgetAreaAmounts uncategorizedAmounts = budgetAreaAmounts.remove(BudgetArea.UNCATEGORIZED);
      if (uncategorizedAmounts != null) {
        values.set(BalanceStat.UNCATEGORIZED, uncategorizedAmounts.getAmount());
      }

      BudgetAreaAmounts expensesAmounts = new BudgetAreaAmounts(BudgetArea.ALL);

      for (Map.Entry<BudgetArea, BudgetAreaAmounts> entry : budgetAreaAmounts.entrySet()) {
        BudgetArea budgetArea = entry.getKey();
        BudgetAreaAmounts amounts = entry.getValue();

        values.set(BalanceStat.getObserved(budgetArea), amounts.getAmount());
        values.set(BalanceStat.getPlanned(budgetArea), amounts.getPlannedAmount());
        values.set(BalanceStat.getRemaining(budgetArea), amounts.getRemainingAmount());
        values.set(BalanceStat.getSummary(budgetArea), amounts.getSummaryAmount());

        if (!budgetArea.isIncome()) {
          expensesAmounts.addValues(amounts);
        }
      }

      values.set(BalanceStat.EXPENSE, expensesAmounts.getAmount());
      values.set(BalanceStat.EXPENSE_PLANNED, expensesAmounts.getPlannedAmount());
      values.set(BalanceStat.EXPENSE_REMAINING, expensesAmounts.getRemainingAmount());
      values.set(BalanceStat.EXPENSE_SUMMARY, expensesAmounts.getSummaryAmount());

      values.set(BalanceStat.SAVINGS_IN, savingsInAmounts.getAmount());
      values.set(BalanceStat.SAVINGS_IN_PLANNED, savingsInAmounts.getPlannedAmount());
      values.set(BalanceStat.SAVINGS_IN_REMAINING, savingsInAmounts.getRemainingAmount());
      values.set(BalanceStat.SAVINGS_IN_SUMMARY, savingsInAmounts.getSummaryAmount());

      values.set(BalanceStat.SAVINGS_IN, savingsOutAmounts.getAmount());
      values.set(BalanceStat.SAVINGS_OUT_PLANNED, savingsOutAmounts.getPlannedAmount());
      values.set(BalanceStat.SAVINGS_OUT_REMAINING, savingsOutAmounts.getRemainingAmount());
      values.set(BalanceStat.SAVINGS_OUT_SUMMARY, savingsOutAmounts.getSummaryAmount());

      double uncategorizedAbs = 0;
      for (Glob transaction : Transaction.getUncategorizedTransactions(monthId, repository)) {
        uncategorizedAbs += Math.abs(transaction.get(Transaction.AMOUNT));
      }
      values.set(BalanceStat.UNCATEGORIZED_ABS, uncategorizedAbs);

      return values.get();
    }
  }

  private class BudgetAreaAmounts {
    private BudgetArea budgetArea;
    private double amount;
    private double plannedAmount;
    private double remainingAmount;
    private double summaryAmount;

    public BudgetAreaAmounts(BudgetArea budgetArea) {
      this.budgetArea = budgetArea;
    }

    public void addValues(Glob seriesStat, int currentMonthId) {
      Double seriesAmount = seriesStat.get(SeriesStat.AMOUNT);
      Double seriesPlannedAmount = seriesStat.get(SeriesStat.PLANNED_AMOUNT);

      amount += seriesAmount;
      plannedAmount += seriesPlannedAmount;

      if (budgetArea.isIncome()) {
        if (amount < plannedAmount) {
          remainingAmount += seriesPlannedAmount - seriesAmount;
        }
      }
      else {
        if (amount > plannedAmount) {
          remainingAmount += seriesPlannedAmount - seriesAmount;
        }
      }

      int monthId = seriesStat.get(SeriesStat.MONTH);
      if (monthId < currentMonthId) {
        summaryAmount += seriesAmount;
      }
      else if (monthId == currentMonthId) {
        summaryAmount += Amounts.max(seriesAmount, seriesPlannedAmount, budgetArea.isIncome());
      }
      else {
        summaryAmount += seriesPlannedAmount;
      }
    }

    public void addValues(BudgetAreaAmounts amounts) {
      this.amount += amounts.amount;
      this.plannedAmount += amounts.plannedAmount;
      this.remainingAmount += amounts.remainingAmount;
      this.summaryAmount += amounts.summaryAmount;
    }

    public Double getAmount() {
      return amount;
    }

    public double getPlannedAmount() {
      return plannedAmount;
    }

    public double getRemainingAmount() {
      return remainingAmount;
    }

    public Double getSummaryAmount() {
      return summaryAmount;
    }
  }
}
