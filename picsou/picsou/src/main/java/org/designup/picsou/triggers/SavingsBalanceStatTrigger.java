package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SavingsBalanceStat;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.MultiMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SavingsBalanceStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(SeriesBudget.TYPE) || changeSet.containsChanges(Transaction.TYPE)) {
      computeStat(repository);
    }
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
      final Map<Integer, GlobBuilder> balanceSummaries = new HashMap<Integer, GlobBuilder>();
      repository.safeApply(SavingsBalanceStat.TYPE, GlobMatchers.ALL, new GlobFunctor() {
        public void run(Glob glob, GlobRepository repository) throws Exception {
          GlobBuilder balance = balanceSummaries.get(glob.get(SavingsBalanceStat.MONTH));
          if (balance == null) {
            balance = GlobBuilder.init(SavingsBalanceStat.TYPE);
            balanceSummaries.put(glob.get(SavingsBalanceStat.MONTH), balance);
          }
          update(glob, balance, SavingsBalanceStat.BEGIN_OF_MONTH_POSITION);
          update(glob, balance, SavingsBalanceStat.END_OF_MONTH_POSITION);
          update(glob, balance, SavingsBalanceStat.BALANCE);
          update(glob, balance, SavingsBalanceStat.LAST_KNOWN_ACCOUNT_POSITION);
          update(glob, balance, SavingsBalanceStat.SAVINGS);
          update(glob, balance, SavingsBalanceStat.SAVINGS_PLANNED);
          update(glob, balance, SavingsBalanceStat.SAVINGS_REMAINING);
          Integer day = glob.get(SavingsBalanceStat.LAST_KNOWN_POSITION_DAY);
          if (day != null) {
            Integer summaryDay = balance.get(SavingsBalanceStat.LAST_KNOWN_POSITION_DAY);
            if (summaryDay == null) {
              balance.set(SavingsBalanceStat.LAST_KNOWN_POSITION_DAY, day);
            }
            else {
              if (summaryDay < day) {
                balance.set(SavingsBalanceStat.LAST_KNOWN_POSITION_DAY, day);
              }
            }
          }
        }
      });
      for (Map.Entry<Integer, GlobBuilder> entry : balanceSummaries.entrySet()) {
        repository.create(Key.create(SavingsBalanceStat.MONTH, entry.getKey(),
                                     SavingsBalanceStat.ACCOUNT, Account.SAVINGS_SUMMARY_ACCOUNT_ID),
                          entry.getValue().toArray());
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private void update(Glob glob, GlobBuilder balance, DoubleField field) {
    Double summaryAmount = balance.get().get(field);
    Double amount = glob.get(field);
    if (summaryAmount == null && amount == null) {
      return;
    }
    balance.set(field, (summaryAmount == null ? 0. : summaryAmount) + (amount == null ? 0. : amount));
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
    MapOfMaps<Integer, Integer, Boolean> isSavingsSeriesAndAccount = new MapOfMaps<Integer, Integer, Boolean>();
    MultiMap<Integer, Integer> accountBySeries = new MultiMap<Integer, Integer>();
    MapOfMaps<Integer, Integer, Glob> firstTransactionForMonth = new MapOfMaps<Integer, Integer, Glob>();
    MapOfMaps<Integer, Integer, Glob> lastTransactionForMonth = new MapOfMaps<Integer, Integer, Glob>();
    Map<Integer, Glob> lastRealKnownTransaction = new HashMap<Integer, Glob>();
    private GlobRepository repository;

    public SavingsFunctor(GlobRepository repository) {
      this.repository = repository;
    }

    public void run(Glob transaction, GlobRepository repository) throws Exception {
      Glob account = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
      if (account == null || !account.get(Account.ACCOUNT_TYPE).equals(AccountType.SAVINGS.getId())) {
        return;
      }
      Integer accountId = account.get(Account.ID);

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

      accountBySeries.putUnique(transaction.get(Transaction.SERIES), accountId);
      Double amount = transaction.get(Transaction.AMOUNT);
      if (!transaction.get(Transaction.PLANNED)) {
        if (amount > 0) {
          data.savings += amount;
          isSavingsSeriesAndAccount.put(transaction.get(Transaction.SERIES), accountId, true);
        }
        else {
          data.out += -amount;
          isSavingsSeriesAndAccount.put(transaction.get(Transaction.SERIES), accountId, false);
        }
      }
      else {
        if (amount > 0) {
          data.savingsRemaining += amount;
          isSavingsSeriesAndAccount.put(transaction.get(Transaction.SERIES), accountId, true);
        }
        else {
          data.outRemaining += -amount;
          isSavingsSeriesAndAccount.put(transaction.get(Transaction.SERIES), accountId, false);
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
      for (Map.Entry<Integer, List<Integer>> seriesToAccounts : accountBySeries.entries()) {
        GlobList months = repository.getAll(Month.TYPE);
        ReadOnlyGlobRepository.MultiFieldIndexed index =
          repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesToAccounts.getKey());
        for (Glob month : months) {
          Glob seriesBudget = index.findByIndex(SeriesBudget.MONTH, month.get(Month.ID)).getGlobs().getFirst();
          for (Integer accountId : seriesToAccounts.getValue()) {
            Boolean isSavings = isSavingsSeriesAndAccount.get(seriesToAccounts.getKey(), accountId);
            SavingsData data = getOrCreateStat(Key.create(SavingsBalanceStat.MONTH, month.get(Month.ID),
                                                          SavingsBalanceStat.ACCOUNT, accountId));
            if (seriesBudget != null) {
              if (isSavings) {
                data.savingsPlanned += Math.abs(seriesBudget.get(SeriesBudget.AMOUNT));
              }
              else {
                data.outPlanned += Math.abs(seriesBudget.get(SeriesBudget.AMOUNT));
              }
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
          endOfMonthPosition = endOfMonthTransaction.get(Transaction.ACCOUNT_POSITION);
          Double amount = beginOfMonthTransaction.get(Transaction.ACCOUNT_POSITION);
          if (amount == null || endOfMonthPosition == null) {
            continue;
          }
          beginOfMonthPosition = amount -
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
                          value(SavingsBalanceStat.END_OF_MONTH_POSITION, endOfMonthPosition));
        if (lastRealKnownTransaction.get(accountId) != null) {
          Integer currentMonthId = lastRealKnownTransaction.get(accountId).get(Transaction.BANK_MONTH);
          if (currentMonthId.equals(monthId)) {
            repository.update(entry.getKey(),
                              value(SavingsBalanceStat.LAST_KNOWN_ACCOUNT_POSITION,
                                    lastRealKnownTransaction.get(accountId).get(Transaction.ACCOUNT_POSITION)),
                              value(SavingsBalanceStat.LAST_KNOWN_POSITION_DAY,
                                    lastRealKnownTransaction.get(accountId).get(Transaction.BANK_DAY)));
          }
        }
      }
    }
  }
}
