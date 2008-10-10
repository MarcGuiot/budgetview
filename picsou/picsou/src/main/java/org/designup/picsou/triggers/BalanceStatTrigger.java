package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
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

  private static class SeriesAmount {
    double income = 0;
    double income_planned = 0;
    double expense = 0;
    double expense_planned = 0;
    double recurring = 0;
    double recurring_planned = 0;
    double envelopes = 0;
    double envelopes_planned = 0;
    double occasional = 0;
    double occasional_planned = 0;
    double special = 0;
    double special_planned = 0;
    double savings = 0;
    double savings_planned = 0;
    double uncategorized = 0;
    double beginOfMonth = 0;
    double endOfMonth = 0;
    double lastKnownBalance = 0;
  }

  private static class BalanceStatCalculator implements GlobFunctor {
    Map<Integer, Glob> firstTransactionForMonth = new HashMap<Integer, Glob>();
    Map<Integer, Glob> lastTransactionForMonth = new HashMap<Integer, Glob>();
    Map<Integer, SeriesAmount> seriesAmount = new HashMap<Integer, SeriesAmount>();
    Glob lastRealKnownTransaction;

    Set<Integer> incomeSeries = new HashSet<Integer>();
    Set<Integer> fixeSeries = new HashSet<Integer>();
    Set<Integer> savingsSeries = new HashSet<Integer>();
    Set<Integer> specialSeries = new HashSet<Integer>();
    Set<Integer> envelopeSeries = new HashSet<Integer>();
    private GlobRepository repository;
    private boolean nullBalance = false;

    private BalanceStatCalculator(GlobRepository repository) {
      this.repository = repository;
      GlobList allSeries = repository.getAll(Series.TYPE);

      for (Glob series : allSeries) {
        if (BudgetArea.INCOME.getId().equals(series.get(Series.BUDGET_AREA))) {
          incomeSeries.add(series.get(Series.ID));
        }
        else if (BudgetArea.RECURRING.getId().equals(series.get(Series.BUDGET_AREA))) {
          fixeSeries.add(series.get(Series.ID));
        }
        else if (BudgetArea.SAVINGS.getId().equals(series.get(Series.BUDGET_AREA))) {
          savingsSeries.add(series.get(Series.ID));
        }
        else if (BudgetArea.SPECIAL.getId().equals(series.get(Series.BUDGET_AREA))) {
          specialSeries.add(series.get(Series.ID));
        }
        else if (BudgetArea.ENVELOPES.getId().equals(series.get(Series.BUDGET_AREA))) {
          envelopeSeries.add(series.get(Series.ID));
        }
      }
    }

    public void run(Glob glob, GlobRepository repository) throws Exception {
      if (glob.get(Transaction.BALANCE) == null) {
        nullBalance = true;
      }
      Integer month = glob.get(Transaction.BANK_MONTH);
      Glob firstTransactionInBankMonth = firstTransactionForMonth.get(month);
      if (firstTransactionInBankMonth == null) {
        firstTransactionForMonth.put(month, glob);
      }
      else {
        if (TransactionComparator.ASCENDING_BANK.compare(glob, firstTransactionInBankMonth) < 0) {
          firstTransactionForMonth.put(month, glob);
        }
      }
      Glob lastTransactionInBankMonth = lastTransactionForMonth.get(month);
      if (lastTransactionInBankMonth == null) {
        lastTransactionForMonth.put(month, glob);
      }
      else {
        if (TransactionComparator.ASCENDING_BANK.compare(glob, lastTransactionInBankMonth) > 0) {
          lastTransactionForMonth.put(month, glob);
        }
      }
      Integer transactionSeries = glob.get(Transaction.SERIES);
      SeriesAmount amount = seriesAmount.get(glob.get(Transaction.BANK_MONTH));
      if (amount == null) {
        amount = new SeriesAmount();
        seriesAmount.put(glob.get(Transaction.BANK_MONTH), amount);
      }

      if (incomeSeries.contains(transactionSeries)) {
        if (glob.get(Transaction.PLANNED)) {
          amount.income_planned += glob.get(Transaction.AMOUNT);
        }
        else {
          amount.income += glob.get(Transaction.AMOUNT);
        }
      }
      else {
        if (fixeSeries.contains(transactionSeries)) {
          if (glob.get(Transaction.PLANNED)) {
            amount.recurring_planned += glob.get(Transaction.AMOUNT);
          }
          else {
            amount.recurring += glob.get(Transaction.AMOUNT);
          }
        }
        else if (envelopeSeries.contains(transactionSeries)) {
          if (glob.get(Transaction.PLANNED)) {
            amount.envelopes_planned += glob.get(Transaction.AMOUNT);
          }
          else {
            amount.envelopes += glob.get(Transaction.AMOUNT);
          }
        }
        else if (specialSeries.contains(transactionSeries)) {
          if (glob.get(Transaction.PLANNED)) {
            amount.special_planned += glob.get(Transaction.AMOUNT);
          }
          else {
            amount.special += glob.get(Transaction.AMOUNT);
          }
        }
        else if (savingsSeries.contains(transactionSeries)) {
          if (glob.get(Transaction.PLANNED)) {
            amount.savings_planned += glob.get(Transaction.AMOUNT);
          }
          else {
            amount.savings += glob.get(Transaction.AMOUNT);
          }
        }
        else if (Series.OCCASIONAL_SERIES_ID.equals(transactionSeries)) {
          if (glob.get(Transaction.PLANNED)) {
            amount.occasional_planned += glob.get(Transaction.AMOUNT);
          }
          else {
            amount.occasional += glob.get(Transaction.AMOUNT);
          }
        }
        else {
          amount.uncategorized += glob.get(Transaction.AMOUNT);
        }
        if (glob.get(Transaction.PLANNED)) {
          amount.expense_planned += glob.get(Transaction.AMOUNT);
        }
        else {
          amount.expense += glob.get(Transaction.AMOUNT);
        }
      }
      if (!glob.get(Transaction.PLANNED) &&
          (lastRealKnownTransaction == null ||
           TransactionComparator.ASCENDING_BANK.compare(glob, lastRealKnownTransaction) > 0)) {
        lastRealKnownTransaction = glob;
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
        SeriesAmount amount = seriesAmount.get(monthId);
        Double balance = beginOfMonthTransaction.get(Transaction.BALANCE);
        double beginOfMonthBalance = balance -
                                     beginOfMonthTransaction.get(Transaction.AMOUNT);
        Double endOfMonthBalance = endOfMonthTransaction.get(Transaction.BALANCE);
        repository.create(Key.create(BalanceStat.TYPE, monthId),
                          FieldValue.value(BalanceStat.MONTH_BALANCE, beginOfMonthBalance - endOfMonthBalance),
                          FieldValue.value(BalanceStat.INCOME, amount.income),
                          FieldValue.value(BalanceStat.INCOME_PLANNED, amount.income_planned),
                          FieldValue.value(BalanceStat.EXPENSE, amount.expense),
                          FieldValue.value(BalanceStat.EXPENSE_PLANNED, amount.expense_planned),
                          FieldValue.value(BalanceStat.OCCASIONAL, amount.occasional),
                          FieldValue.value(BalanceStat.OCCASIONAL_PLANNED, amount.occasional_planned),
                          FieldValue.value(BalanceStat.RECURRING, amount.recurring),
                          FieldValue.value(BalanceStat.RECURRING_PLANNED, amount.recurring_planned),
                          FieldValue.value(BalanceStat.ENVELOPES, amount.envelopes),
                          FieldValue.value(BalanceStat.ENVELOPES_PLANNED, amount.envelopes_planned),
                          FieldValue.value(BalanceStat.SAVINGS, amount.savings),
                          FieldValue.value(BalanceStat.SAVINGS_PLANNED, amount.savings_planned),
                          FieldValue.value(BalanceStat.SPECIAL, amount.special),
                          FieldValue.value(BalanceStat.SPECIAL_PLANNED, amount.special_planned),
                          FieldValue.value(BalanceStat.UNCATEGORIZED, amount.uncategorized),
                          FieldValue.value(BalanceStat.BEGIN_OF_MONTH_ACCOUNT_BALANCE, beginOfMonthBalance),
                          FieldValue.value(BalanceStat.END_OF_MONTH_ACCOUNT_BALANCE, endOfMonthBalance)
        );
        if (lastRealKnownTransaction != null) {
          Integer currentMonthId = lastRealKnownTransaction.get(Transaction.BANK_MONTH);
          if (currentMonthId.equals(monthId)) {
            repository.update(Key.create(BalanceStat.TYPE, currentMonthId),
                              FieldValue.value(
                                BalanceStat.LAST_KNOWN_ACCOUNT_BALANCE,
                                lastRealKnownTransaction.get(Transaction.BALANCE)),
                              FieldValue.value(
                                BalanceStat.LAST_KNOWN_ACCOUNT_BALANCE_DAY,
                                lastRealKnownTransaction.get(Transaction.BANK_DAY)));
          }
        }
      }
    }
  }
}
