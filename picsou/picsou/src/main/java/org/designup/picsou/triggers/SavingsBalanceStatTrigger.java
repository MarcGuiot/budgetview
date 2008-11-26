package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SavingsBalanceStat;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.MapOfMaps;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SavingsBalanceStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    computeStat(repository);
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    computeStat(repository);
  }

  private void computeStat(GlobRepository repository) {
    SavingsFunctor callback = new SavingsFunctor(repository);
    repository.startChangeSet();
    try {
      repository.deleteAll(SavingsBalanceStat.TYPE);
      repository.safeApply(Transaction.TYPE, GlobMatchers.ALL, callback);
      callback.complete();
    }
    finally {
      repository.completeChangeSet();
    }
  }


  private static class SavingsData {
    public double out;
    public double outRemaining;
    public double outPlanned;
    public double savings;
    public double savingsRemaining;
    public double savingsPlanned;
  }

  private static class SavingsFunctor implements GlobFunctor {
    private Map<Key, SavingsData> computedData = new HashMap<Key, SavingsData>();
    Map<Integer, Boolean> isSavingsSeries = new HashMap<Integer, Boolean>();
    Map<Integer, Integer> acountBySeries = new HashMap<Integer, Integer>();
    MapOfMaps<Integer, Integer, Glob> firstTransactionForMonth = new MapOfMaps<Integer, Integer, Glob>();
    MapOfMaps<Integer, Integer, Glob> lastTransactionForMonth = new MapOfMaps<Integer, Integer, Glob>();
    Map<Integer, Glob> lastRealKnownTransaction = new HashMap<Integer, Glob>();
    private SameAccountChecker savingsAccountChecker;
    private GlobRepository repository;

    public SavingsFunctor(GlobRepository repository) {
      this.repository = repository;
      savingsAccountChecker = SameAccountChecker.getSameAsSavings(repository);
    }

    public void run(Glob transaction, GlobRepository repository) throws Exception {
      Integer accountId = transaction.get(Transaction.ACCOUNT);
      if (!savingsAccountChecker.isSame(accountId)) {
        return;
      }

      Integer monthId = transaction.get(Transaction.BANK_MONTH);
      Glob firstTransactionInBankMonth = firstTransactionForMonth.get(accountId, monthId);
      if ((firstTransactionInBankMonth == null) ||
          (TransactionComparator.ASCENDING_BANK.compare(transaction, firstTransactionInBankMonth) < 0)) {
        firstTransactionForMonth.put(accountId, monthId, transaction);
      }

      Glob lastTransactionInBankMonth = lastTransactionForMonth.get(accountId, monthId);
      if ((lastTransactionInBankMonth == null)
          || (TransactionComparator.ASCENDING_BANK.compare(transaction, lastTransactionInBankMonth) > 0)) {
        lastTransactionForMonth.put(accountId, monthId, transaction);
      }


      Key key = Key.create(SavingsBalanceStat.MONTH, transaction.get(Transaction.MONTH),
                           SavingsBalanceStat.ACCOUNT, accountId);
      SavingsData data = getOrCreateStat(key);

      Integer seriesId = transaction.get(Transaction.SERIES);
      acountBySeries.put(transaction.get(Transaction.SERIES), accountId);
      Boolean isSavingsSeries = this.isSavingsSeries.get(seriesId);
      if (isSavingsSeries == null) {
        Glob series = repository.get(Key.create(Series.TYPE, seriesId));
        isSavingsSeries = series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId());
        this.isSavingsSeries.put(seriesId, isSavingsSeries);
      }
      Double amount = transaction.get(Transaction.AMOUNT);
      if (!transaction.get(Transaction.PLANNED)) {
        if (isSavingsSeries) {
          data.savings += amount;
        }
        else {
          data.out += amount;
        }
      }
      else {
        if (isSavingsSeries) {
          data.savingsRemaining += amount;
        }
        else {
          data.outRemaining += amount;
        }
      }
      if (!transaction.get(Transaction.PLANNED) &&
          (lastRealKnownTransaction.get(accountId) == null ||
           TransactionComparator.ASCENDING_BANK.compare(transaction, lastRealKnownTransaction.get(accountId)) > 0)) {
        lastRealKnownTransaction.put(accountId, transaction);
      }
    }

    private SavingsData getOrCreateStat(Key key) {
      SavingsData data = this.computedData.get(key);
      if (data == null) {
        data = new SavingsData();
        this.computedData.put(key, data);
      }
      return data;
    }

    void complete() {
      for (Map.Entry<Integer, Integer> entry : acountBySeries.entrySet()) {
        GlobList months = repository.getAll(Month.TYPE);
        ReadOnlyGlobRepository.MultiFieldIndexed index =
          repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, entry.getKey());
        for (Glob month : months) {
          Glob seriesBudget = index.findByIndex(SeriesBudget.MONTH, month.get(Month.ID)).getGlobs().getFirst();
          SavingsData data = getOrCreateStat(Key.create(SavingsBalanceStat.MONTH, month.get(Month.ID),
                                                        SavingsBalanceStat.ACCOUNT, entry.getValue()));
          if (seriesBudget != null) {
            if (isSavingsSeries.get(entry.getKey())) {
              data.savingsPlanned += seriesBudget.get(SeriesBudget.AMOUNT);
            }
            else {
              data.outPlanned += seriesBudget.get(SeriesBudget.AMOUNT);
            }
          }
        }
      }
      for (Map.Entry<Key, SavingsData> entry : computedData.entrySet()) {
        Integer monthId = entry.getKey().get(SavingsBalanceStat.MONTH);
        Integer accountId = entry.getKey().get(SavingsBalanceStat.ACCOUNT);

        Glob beginOfMonthTransaction = firstTransactionForMonth.get(accountId, monthId);
        Glob endOfMonthTransaction = lastTransactionForMonth.get(accountId, monthId);

        Double beginOfMonthPosition = null;
        Double balance = null;
        Double endOfMonthPosition = null;
        if (beginOfMonthTransaction != null && endOfMonthTransaction != null) {
          endOfMonthPosition = endOfMonthTransaction.get(Transaction.BALANCE);
          beginOfMonthPosition = beginOfMonthTransaction.get(Transaction.BALANCE) -
                                 beginOfMonthTransaction.get(Transaction.AMOUNT);
          balance = endOfMonthPosition - beginOfMonthPosition;
        }

        repository.create(entry.getKey(),
                          FieldValue.value(SavingsBalanceStat.BALANCE, balance),
                          FieldValue.value(SavingsBalanceStat.OUT, entry.getValue().out),
                          FieldValue.value(SavingsBalanceStat.OUT_PLANNED, entry.getValue().outPlanned),
                          FieldValue.value(SavingsBalanceStat.OUT_REMAINING, entry.getValue().outRemaining),
                          FieldValue.value(SavingsBalanceStat.SAVINGS, entry.getValue().savings),
                          FieldValue.value(SavingsBalanceStat.SAVINGS_PLANNED, entry.getValue().savingsPlanned),
                          FieldValue.value(SavingsBalanceStat.SAVINGS_REMAINING, entry.getValue().savingsRemaining),
                          value(SavingsBalanceStat.BEGIN_OF_MONTH_POSITION, beginOfMonthPosition),
                          value(SavingsBalanceStat.END_OF_MONTH_POSITION, endOfMonthPosition)
        );
        if (lastRealKnownTransaction.get(accountId) != null) {
          Integer currentMonthId = lastRealKnownTransaction.get(accountId).get(Transaction.BANK_MONTH);
          if (currentMonthId.equals(monthId)) {
            repository.update(entry.getKey(),
                              value(SavingsBalanceStat.LAST_KNOWN_ACCOUNT_POSITION,
                                    lastRealKnownTransaction.get(accountId).get(Transaction.BALANCE)),
                              value(SavingsBalanceStat.LAST_KNOWN_POSITION_DAY,
                                    lastRealKnownTransaction.get(accountId).get(Transaction.BANK_DAY)));
          }
        }
      }
    }
  }
}
