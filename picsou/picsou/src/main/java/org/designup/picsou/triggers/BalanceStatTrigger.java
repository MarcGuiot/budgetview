package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.model.*;
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
      if (!balanceStatCalculator.nullBalance) {
        balanceStatCalculator.createStat();
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private static class SeriesAmounts {
    private double income = 0;
    private double remainingIncome = 0;
    private double plannedIncome = 0;
    private double expenses = 0;
    private double remaningExpenses = 0;
    private double plannedExpenses = 0;
    private double recurring = 0;
    private double remaningRecurring = 0;
    private double plannedRecurring = 0;
    private double envelopes = 0;
    private double remaningEnvelopes = 0;
    private double plannedEnvelopes = 0;
    private double occasional = 0;
    private double remaningOccasional = 0;
    private double plannedOccasional = 0;
    private double special = 0;
    private double remaningSpecial = 0;
    private double plannedSpecial = 0;
    private double savings = 0;
    private double remaningSavings = 0;
    private double plannedSavings = 0;
    private double savings_in = 0;
    private double remaningSavings_in = 0;
    private double plannedSavings_in = 0;
    private double savings_out = 0;
    private double remaningSavings_out = 0;
    private double plannedSavings_out = 0;
    private double uncategorized = 0;
    private double beginOfMonth = 0;
    private double endOfMonth = 0;
  }

  private static class BalanceStatCalculator implements GlobFunctor {
    private Map<Integer, Glob> firstTransactionForMonth = new HashMap<Integer, Glob>();
    private Map<Integer, Glob> lastTransactionForMonth = new HashMap<Integer, Glob>();
    private Map<Integer, SeriesAmounts> monthSeriesAmounts = new HashMap<Integer, SeriesAmounts>();
    private Glob lastRealKnownTransaction;

    private Set<Integer> incomeSeries = new HashSet<Integer>();
    private Set<Integer> fixedSeries = new HashSet<Integer>();
    private Set<Integer> savingsSeries = new HashSet<Integer>();
    private Set<Integer> specialSeries = new HashSet<Integer>();
    private Set<Integer> envelopeSeries = new HashSet<Integer>();

    private GlobRepository repository;
    private boolean nullBalance = false;
    private SameAccountChecker mainAccountChecker;

    private BalanceStatCalculator(GlobRepository repository) {
      this.repository = repository;

      mainAccountChecker = SameAccountChecker.getSameAsMain(repository);
      for (Glob series : repository.getAll(Series.TYPE)) {
        Integer seriesId = series.get(Series.ID);
        if (BudgetArea.INCOME.getId().equals(series.get(Series.BUDGET_AREA))) {
          incomeSeries.add(seriesId);
          GlobList budgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
            .getGlobs();
          for (Glob budget : budgets) {
            SeriesAmounts amounts = getOrCreate(budget.get(SeriesBudget.MONTH));
            amounts.plannedIncome += budget.get(SeriesBudget.AMOUNT);
          }
        }
        else if (BudgetArea.RECURRING.getId().equals(series.get(Series.BUDGET_AREA))) {
          fixedSeries.add(seriesId);
          GlobList budgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
            .getGlobs();
          for (Glob budget : budgets) {
            SeriesAmounts amounts = getOrCreate(budget.get(SeriesBudget.MONTH));
            amounts.plannedRecurring += budget.get(SeriesBudget.AMOUNT);
            amounts.plannedExpenses += budget.get(SeriesBudget.AMOUNT);
          }
        }
        else if (BudgetArea.ENVELOPES.getId().equals(series.get(Series.BUDGET_AREA))) {
          envelopeSeries.add(seriesId);
          GlobList budgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
            .getGlobs();
          for (Glob budget : budgets) {
            SeriesAmounts amounts = getOrCreate(budget.get(SeriesBudget.MONTH));
            amounts.plannedEnvelopes += budget.get(SeriesBudget.AMOUNT);
            amounts.plannedExpenses += budget.get(SeriesBudget.AMOUNT);
          }
        }
        else if (BudgetArea.SAVINGS.getId().equals(series.get(Series.BUDGET_AREA))) {
          if (!(mainAccountChecker.isSame(series.get(Series.FROM_ACCOUNT))
                || mainAccountChecker.isSame(series.get(Series.TO_ACCOUNT)))) {
            continue;
          }
          if (series.get(Series.MIRROR_SERIES) != null) {
            if (mainAccountChecker.isSame(series.get(Series.FROM_ACCOUNT)) && !series.get(Series.IS_MIRROR)) {
              continue;
            }
            if (mainAccountChecker.isSame(series.get(Series.TO_ACCOUNT)) && series.get(Series.IS_MIRROR)) {
              continue;
            }
          }

          savingsSeries.add(seriesId);
          GlobList budgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
            .getGlobs();
          for (Glob budget : budgets) {
            SeriesAmounts amounts = getOrCreate(budget.get(SeriesBudget.MONTH));
            Double amount = budget.get(SeriesBudget.AMOUNT);
            amounts.plannedSavings += amount;
            if (amount < 0){
              amounts.plannedSavings_in += amount;
            }else{
              amounts.plannedSavings_out += amount;
            }
            amounts.plannedExpenses += amount;
          }
        }
        else if (BudgetArea.SPECIAL.getId().equals(series.get(Series.BUDGET_AREA))) {
          specialSeries.add(seriesId);
          GlobList budgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
            .getGlobs();
          for (Glob budget : budgets) {
            SeriesAmounts amounts = getOrCreate(budget.get(SeriesBudget.MONTH));
            amounts.plannedSpecial += budget.get(SeriesBudget.AMOUNT);
            amounts.plannedExpenses += budget.get(SeriesBudget.AMOUNT);
          }
        }
        else if (BudgetArea.OCCASIONAL.getId().equals(series.get(Series.BUDGET_AREA))) {
          GlobList budgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
            .getGlobs();
          for (Glob budget : budgets) {
            SeriesAmounts amounts = getOrCreate(budget.get(SeriesBudget.MONTH));
            amounts.plannedOccasional += budget.get(SeriesBudget.AMOUNT);
            amounts.plannedExpenses += budget.get(SeriesBudget.AMOUNT);
          }
        }
      }
    }

    public void run(Glob transaction, GlobRepository repository) throws Exception {
      if (transaction.get(Transaction.SUMMARY_POSITION) == null) {
        nullBalance = true;
      }

      if (!mainAccountChecker.isSame(transaction.get(Transaction.ACCOUNT))) {
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

      Integer transactionSeries = transaction.get(Transaction.SERIES);
      SeriesAmounts amounts = getOrCreate(transaction.get(Transaction.MONTH));

      Double amount = transaction.get(Transaction.AMOUNT);

      if (incomeSeries.contains(transactionSeries)) {
        if (transaction.get(Transaction.PLANNED)) {
          amounts.remainingIncome += amount;
        }
        else {
          amounts.income += amount;
        }
      }
      else {
        if (fixedSeries.contains(transactionSeries)) {
          if (transaction.get(Transaction.PLANNED)) {
            amounts.remaningRecurring += amount;
          }
          else {
            amounts.recurring += amount;
          }
        }
        else if (envelopeSeries.contains(transactionSeries)) {
          if (transaction.get(Transaction.PLANNED)) {
            amounts.remaningEnvelopes += amount;
          }
          else {
            amounts.envelopes += amount;
          }
        }
        else if (specialSeries.contains(transactionSeries)) {
          if (transaction.get(Transaction.PLANNED)) {
            amounts.remaningSpecial += amount;
          }
          else {
            amounts.special += amount;
          }
        }
        else if (savingsSeries.contains(transactionSeries)) {
          if (transaction.get(Transaction.PLANNED)) {
            amounts.remaningSavings += amount;
            if (amount < 0){
              amounts.remaningSavings_in += amount;
            }else{
              amounts.remaningSavings_out += amount;
            }
          }
          else {
            amounts.savings += amount;
            if (amount < 0){
              amounts.savings_in += amount;
            }else{
              amounts.savings_out += amount;
            }
          }
        }
        else if (Series.OCCASIONAL_SERIES_ID.equals(transactionSeries)) {
          if (transaction.get(Transaction.PLANNED)) {
            amounts.remaningOccasional += amount;
          }
          else {
            amounts.occasional += amount;
          }
        }
        else {
          amounts.uncategorized += Math.abs(amount);
        }

        if (transaction.get(Transaction.PLANNED)) {
          amounts.remaningExpenses += amount;
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

    private SeriesAmounts getOrCreate(Integer monthId) {
      SeriesAmounts amounts = monthSeriesAmounts.get(monthId);
      if (amounts == null) {
        amounts = new SeriesAmounts();
        monthSeriesAmounts.put(monthId, amounts);
      }
      return amounts;
    }

    void createStat() {
      GlobList months = repository.getAll(Month.TYPE).sort(Month.ID);
      for (Glob month : months) {
        Integer monthId = month.get(Month.ID);
        Glob beginOfMonthTransaction = firstTransactionForMonth.get(monthId);
        Glob endOfMonthTransaction = lastTransactionForMonth.get(monthId);

        Double beginOfMonthPosition = null;
        Double balance = null;
        Double endOfMonthPosition = null;
        if (beginOfMonthTransaction != null && endOfMonthTransaction != null) {
          endOfMonthPosition = endOfMonthTransaction.get(Transaction.SUMMARY_POSITION);
          beginOfMonthPosition = beginOfMonthTransaction.get(Transaction.SUMMARY_POSITION) -
                                 beginOfMonthTransaction.get(Transaction.AMOUNT);
          balance = endOfMonthPosition - beginOfMonthPosition;
        }


        SeriesAmounts seriesAmounts = monthSeriesAmounts.get(monthId);

        if (seriesAmounts == null) {
          seriesAmounts = new SeriesAmounts();
        }

        repository.create(Key.create(BalanceStat.TYPE, monthId),
                          value(BalanceStat.MONTH_BALANCE, balance),

                          value(BalanceStat.INCOME, seriesAmounts.income),
                          value(BalanceStat.INCOME_REMAINING, seriesAmounts.remainingIncome),
                          value(BalanceStat.INCOME_PLANNED, seriesAmounts.plannedIncome),

                          value(BalanceStat.EXPENSE, seriesAmounts.expenses),
                          value(BalanceStat.EXPENSE_REMAINING, seriesAmounts.remaningExpenses),
                          value(BalanceStat.EXPENSE_PLANNED, seriesAmounts.plannedExpenses),

                          value(BalanceStat.OCCASIONAL, seriesAmounts.occasional),
                          value(BalanceStat.OCCASIONAL_REMAINING, seriesAmounts.remaningOccasional),
                          value(BalanceStat.OCCASIONAL_PLANNED, seriesAmounts.plannedOccasional),

                          value(BalanceStat.RECURRING, seriesAmounts.recurring),
                          value(BalanceStat.RECURRING_REMAINING, seriesAmounts.remaningRecurring),
                          value(BalanceStat.RECURRING_PLANNED, seriesAmounts.plannedRecurring),

                          value(BalanceStat.ENVELOPES, seriesAmounts.envelopes),
                          value(BalanceStat.ENVELOPES_REMAINING, seriesAmounts.remaningEnvelopes),
                          value(BalanceStat.ENVELOPES_PLANNED, seriesAmounts.plannedEnvelopes),

                          value(BalanceStat.SAVINGS, seriesAmounts.savings),
                          value(BalanceStat.SAVINGS_REMAINING, seriesAmounts.remaningSavings),
                          value(BalanceStat.SAVINGS_PLANNED, seriesAmounts.plannedSavings),

                          value(BalanceStat.SAVINGS_IN, seriesAmounts.savings_in),
                          value(BalanceStat.SAVINGS_REMAINING_IN, seriesAmounts.remaningSavings_in),
                          value(BalanceStat.SAVINGS_PLANNED_IN, seriesAmounts.plannedSavings_in),

                          value(BalanceStat.SAVINGS_OUT, seriesAmounts.savings_out),
                          value(BalanceStat.SAVINGS_REMAINING_OUT, seriesAmounts.remaningSavings_out),
                          value(BalanceStat.SAVINGS_PLANNED_OUT, seriesAmounts.plannedSavings_out),

                          value(BalanceStat.SPECIAL, seriesAmounts.special),
                          value(BalanceStat.SPECIAL_REMAINING, seriesAmounts.remaningSpecial),
                          value(BalanceStat.SPECIAL_PLANNED, seriesAmounts.plannedSpecial),

                          value(BalanceStat.UNCATEGORIZED, seriesAmounts.uncategorized),
                          value(BalanceStat.BEGIN_OF_MONTH_ACCOUNT_POSITION, beginOfMonthPosition),
                          value(BalanceStat.END_OF_MONTH_ACCOUNT_POSITION, endOfMonthPosition)
        );
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
  }
}
