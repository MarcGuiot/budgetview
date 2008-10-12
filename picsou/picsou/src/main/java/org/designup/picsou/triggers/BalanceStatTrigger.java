package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BalanceStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE)) {
      computeStat(repository);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    computeStat(repository);
  }

  private void computeStat(GlobRepository repository) {
    repository.enterBulkDispatchingMode();
    try {
      repository.deleteAll(BalanceStat.TYPE);
      BalanceStatCalculator balanceStatCalculator = new BalanceStatCalculator(repository);
      repository.safeApply(Transaction.TYPE, GlobMatchers.ALL, balanceStatCalculator);
      if (!balanceStatCalculator.nullBalance) {
        balanceStatCalculator.createStat();
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  private static class SeriesAmounts {
    double income = 0;
    double plannedIncome = 0;
    double expenses = 0;
    double plannedExpenses = 0;
    double recurring = 0;
    double plannedRecurring = 0;
    double envelopes = 0;
    double plannedEnvelopes = 0;
    double occasional = 0;
    double plannedOccasional = 0;
    double special = 0;
    double plannedSpecial = 0;
    double savings = 0;
    double plannedSavings = 0;
    double uncategorized = 0;
    double beginOfMonth = 0;
    double endOfMonth = 0;
    double lastKnownBalance = 0;
  }

  private static class BalanceStatCalculator implements GlobFunctor {
    Map<Integer, Glob> firstTransactionForMonth = new HashMap<Integer, Glob>();
    Map<Integer, Glob> lastTransactionForMonth = new HashMap<Integer, Glob>();
    Map<Integer, SeriesAmounts> monthSeriesAmounts = new HashMap<Integer, SeriesAmounts>();
    Glob lastRealKnownTransaction;

    Set<Integer> incomeSeries = new HashSet<Integer>();
    Set<Integer> fixedSeries = new HashSet<Integer>();
    Set<Integer> savingsSeries = new HashSet<Integer>();
    Set<Integer> specialSeries = new HashSet<Integer>();
    Set<Integer> envelopeSeries = new HashSet<Integer>();

    private GlobRepository repository;
    private boolean nullBalance = false;

    private BalanceStatCalculator(GlobRepository repository) {
      this.repository = repository;

      for (Glob series : repository.getAll(Series.TYPE)) {
        if (BudgetArea.INCOME.getId().equals(series.get(Series.BUDGET_AREA))) {
          incomeSeries.add(series.get(Series.ID));
        }
        else if (BudgetArea.RECURRING.getId().equals(series.get(Series.BUDGET_AREA))) {
          fixedSeries.add(series.get(Series.ID));
        }
        else if (BudgetArea.ENVELOPES.getId().equals(series.get(Series.BUDGET_AREA))) {
          envelopeSeries.add(series.get(Series.ID));
        }
        else if (BudgetArea.SAVINGS.getId().equals(series.get(Series.BUDGET_AREA))) {
          savingsSeries.add(series.get(Series.ID));
        }
        else if (BudgetArea.SPECIAL.getId().equals(series.get(Series.BUDGET_AREA))) {
          specialSeries.add(series.get(Series.ID));
        }
      }
    }

    public void run(Glob transaction, GlobRepository repository) throws Exception {
      if (transaction.get(Transaction.BALANCE) == null) {
        nullBalance = true;
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

      Integer transactionSeries = transaction.get(Transaction.SERIES);
      SeriesAmounts amounts = monthSeriesAmounts.get(transaction.get(Transaction.BANK_MONTH));
      if (amounts == null) {
        amounts = new SeriesAmounts();
        monthSeriesAmounts.put(monthId, amounts);
      }

      Double amount = transaction.get(Transaction.AMOUNT);

      if (incomeSeries.contains(transactionSeries)) {
        if (transaction.get(Transaction.PLANNED)) {
          amounts.plannedIncome += amount;
        }
        else {
          amounts.income += amount;
        }
      }
      else {
        if (fixedSeries.contains(transactionSeries)) {
          if (transaction.get(Transaction.PLANNED)) {
            amounts.plannedRecurring += amount;
          }
          else {
            amounts.recurring += amount;
          }
        }
        else if (envelopeSeries.contains(transactionSeries)) {
          if (transaction.get(Transaction.PLANNED)) {
            amounts.plannedEnvelopes += amount;
          }
          else {
            amounts.envelopes += amount;
          }
        }
        else if (specialSeries.contains(transactionSeries)) {
          if (transaction.get(Transaction.PLANNED)) {
            amounts.plannedSpecial += amount;
          }
          else {
            amounts.special += amount;
          }
        }
        else if (savingsSeries.contains(transactionSeries)) {
          if (transaction.get(Transaction.PLANNED)) {
            amounts.plannedSavings += amount;
          }
          else {
            amounts.savings += amount;
          }
        }
        else if (Series.OCCASIONAL_SERIES_ID.equals(transactionSeries)) {
          if (transaction.get(Transaction.PLANNED)) {
            amounts.plannedOccasional += amount;
          }
          else {
            amounts.occasional += amount;
          }
        }
        else {
          amounts.uncategorized += amount;
        }

        if (transaction.get(Transaction.PLANNED)) {
          amounts.plannedExpenses += amount;
        }
        else {
          amounts.expenses += amount;
        }
      }
      if (!transaction.get(Transaction.PLANNED) &&
          (lastRealKnownTransaction == null ||
           TransactionComparator.ASCENDING_BANK.compare(transaction, lastRealKnownTransaction) > 0)) {
        lastRealKnownTransaction = transaction;
      }
    }

    void createStat() {
      for (Glob month : repository.getAll(Month.TYPE)) {

        Integer monthId = month.get(Month.ID);
        Glob beginOfMonthTransaction = firstTransactionForMonth.get(monthId);
        Glob endOfMonthTransaction = lastTransactionForMonth.get(monthId);
        if (beginOfMonthTransaction == null) {
          repository.create(Key.create(BalanceStat.TYPE, monthId));
          continue;
        }

        SeriesAmounts seriesAmounts = monthSeriesAmounts.get(monthId);

        double beginOfMonthBalance =
          beginOfMonthTransaction.get(Transaction.BALANCE) -
          beginOfMonthTransaction.get(Transaction.AMOUNT);

        Double endOfMonthBalance = endOfMonthTransaction.get(Transaction.BALANCE);

        repository.create(Key.create(BalanceStat.TYPE, monthId),
                          value(BalanceStat.MONTH_BALANCE, endOfMonthBalance - beginOfMonthBalance),
                          value(BalanceStat.INCOME, seriesAmounts.income),
                          value(BalanceStat.INCOME_REMAINING, seriesAmounts.plannedIncome),
                          value(BalanceStat.EXPENSE, seriesAmounts.expenses),
                          value(BalanceStat.EXPENSE_REMAINING, seriesAmounts.plannedExpenses),
                          value(BalanceStat.OCCASIONAL, seriesAmounts.occasional),
                          value(BalanceStat.OCCASIONAL_REMAINING, seriesAmounts.plannedOccasional),
                          value(BalanceStat.RECURRING, seriesAmounts.recurring),
                          value(BalanceStat.RECURRING_REMAINING, seriesAmounts.plannedRecurring),
                          value(BalanceStat.ENVELOPES, seriesAmounts.envelopes),
                          value(BalanceStat.ENVELOPES_REMAINING, seriesAmounts.plannedEnvelopes),
                          value(BalanceStat.SAVINGS, seriesAmounts.savings),
                          value(BalanceStat.SAVINGS_REMAINING, seriesAmounts.plannedSavings),
                          value(BalanceStat.SPECIAL, seriesAmounts.special),
                          value(BalanceStat.SPECIAL_REMAINING, seriesAmounts.plannedSpecial),
                          value(BalanceStat.UNCATEGORIZED, seriesAmounts.uncategorized),
                          value(BalanceStat.BEGIN_OF_MONTH_ACCOUNT_BALANCE, beginOfMonthBalance),
                          value(BalanceStat.END_OF_MONTH_ACCOUNT_BALANCE, endOfMonthBalance)
        );
        if (lastRealKnownTransaction != null) {
          Integer currentMonthId = lastRealKnownTransaction.get(Transaction.BANK_MONTH);
          if (currentMonthId.equals(monthId)) {
            repository.update(Key.create(BalanceStat.TYPE, currentMonthId),
                              value(
                                BalanceStat.LAST_KNOWN_ACCOUNT_BALANCE,
                                lastRealKnownTransaction.get(Transaction.BALANCE)),
                              value(
                                BalanceStat.LAST_KNOWN_ACCOUNT_BALANCE_DAY,
                                lastRealKnownTransaction.get(Transaction.BANK_DAY)));
          }
        }
      }
    }
  }
}
