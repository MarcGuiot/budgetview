package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;

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
      balanceStatCalculator.createStat();
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
    private Glob currentMonth;
    private Glob absolutFirstTransaction;

    private Set<Integer> incomeSeries = new HashSet<Integer>();
    private Set<Integer> fixedSeries = new HashSet<Integer>();
    private Set<Integer> savingsSeries = new HashSet<Integer>();
    private Set<Integer> specialSeries = new HashSet<Integer>();
    private Set<Integer> envelopeSeries = new HashSet<Integer>();

    private GlobRepository repository;

    private BalanceStatCalculator(GlobRepository repository) {
      this.repository = repository;
      currentMonth = repository.get(CurrentMonth.KEY);
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
          Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
          Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
          if (!(fromAccount != null && fromAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())
                || (toAccount != null && toAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())))) {
            continue;
          }
          if (series.get(Series.MIRROR_SERIES) != null) {
            if (fromAccount != null && fromAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId()) && !series.get(Series.IS_MIRROR)) {
              continue;
            }
            if (toAccount != null && toAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId()) && series.get(Series.IS_MIRROR)) {
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
            if (amount < 0) {
              amounts.plannedSavings_in += amount;
            }
            else {
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
      }
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

      if (absolutFirstTransaction == null ||
          (TransactionComparator.ASCENDING_BANK.compare(transaction, absolutFirstTransaction) < 0)) {
        absolutFirstTransaction = transaction;

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
            if (amount < 0) {
              amounts.remaningSavings_in += amount;
            }
            else {
              amounts.remaningSavings_out += amount;
            }
          }
          else {
            amounts.savings += amount;
            if (amount < 0) {
              amounts.savings_in += amount;
            }
            else {
              amounts.savings_out += amount;
            }
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
           TransactionComparator.ASCENDING_BANK.compare(transaction, lastRealKnownTransaction) > 0)
          && transaction.get(Transaction.BANK_MONTH).equals(currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH))
          && transaction.get(Transaction.BANK_DAY) <= currentMonth.get(CurrentMonth.LAST_TRANSACTION_DAY)) {
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
          endOfMonthTransaction = absolutFirstTransaction;
          beginOfMonthTransaction = absolutFirstTransaction;
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

        Double balance = null;
        if (beginOfMonthPosition != null) {
          balance = 0.;
        }
        else if (beginOfMonthTransaction != null && endOfMonthTransaction != null) {
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
              if (endOfMonthPosition != null && beginOfMonthPosition != null) {
                balance = endOfMonthPosition - beginOfMonthPosition;
              }
            }
          }
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
