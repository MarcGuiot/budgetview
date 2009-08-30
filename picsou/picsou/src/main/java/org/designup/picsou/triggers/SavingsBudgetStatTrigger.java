package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SavingsBudgetStat;
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

public class SavingsBudgetStatTrigger implements ChangeSetListener {
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
      repository.deleteAll(SavingsBudgetStat.TYPE);
      repository.safeApply(Transaction.TYPE, GlobMatchers.ALL, callback);
      callback.complete();
      final Map<Integer, GlobBuilder> balanceSummaries = new HashMap<Integer, GlobBuilder>();
      repository.safeApply(SavingsBudgetStat.TYPE, GlobMatchers.ALL, new GlobFunctor() {
        public void run(Glob glob, GlobRepository repository) throws Exception {
          GlobBuilder balance = balanceSummaries.get(glob.get(SavingsBudgetStat.MONTH));
          if (balance == null) {
            balance = GlobBuilder.init(SavingsBudgetStat.TYPE);
            balanceSummaries.put(glob.get(SavingsBudgetStat.MONTH), balance);
          }
          update(glob, balance, SavingsBudgetStat.BEGIN_OF_MONTH_POSITION);
          update(glob, balance, SavingsBudgetStat.END_OF_MONTH_POSITION);
          update(glob, balance, SavingsBudgetStat.BALANCE);
          update(glob, balance, SavingsBudgetStat.LAST_KNOWN_ACCOUNT_POSITION);
          update(glob, balance, SavingsBudgetStat.SAVINGS);
          update(glob, balance, SavingsBudgetStat.SAVINGS_PLANNED);
          update(glob, balance, SavingsBudgetStat.SAVINGS_REMAINING);
          Integer day = glob.get(SavingsBudgetStat.LAST_KNOWN_POSITION_DAY);
          if (day != null) {
            Integer summaryDay = balance.get(SavingsBudgetStat.LAST_KNOWN_POSITION_DAY);
            if (summaryDay == null) {
              balance.set(SavingsBudgetStat.LAST_KNOWN_POSITION_DAY, day);
            }
            else {
              if (summaryDay < day) {
                balance.set(SavingsBudgetStat.LAST_KNOWN_POSITION_DAY, day);
              }
            }
          }
        }
      });
      for (Map.Entry<Integer, GlobBuilder> entry : balanceSummaries.entrySet()) {
        repository.create(Key.create(SavingsBudgetStat.MONTH, entry.getKey(),
                                     SavingsBudgetStat.ACCOUNT, Account.SAVINGS_SUMMARY_ACCOUNT_ID),
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
    private MapOfMaps<Integer, Integer, SavingsData> computedData = new MapOfMaps<Integer, Integer, SavingsData>();
    private MapOfMaps<Integer, Integer, Boolean> isSavingsSeriesAndAccount = new MapOfMaps<Integer, Integer, Boolean>();
    private MultiMap<Integer, Integer> accountBySeries = new MultiMap<Integer, Integer>();
    private MapOfMaps<Integer, Integer, Glob> firstTransactionForMonth = new MapOfMaps<Integer, Integer, Glob>();
    private MapOfMaps<Integer, Integer, Glob> lastTransactionForMonth = new MapOfMaps<Integer, Integer, Glob>();
    private Map<Integer, Glob> absoluteFirstTransactionForMonth = new HashMap<Integer, Glob>();
    private Map<Integer, Glob> lastRealKnownTransaction = new HashMap<Integer, Glob>();
    private GlobRepository repository;
    private Glob currentMonth;

    public SavingsFunctor(GlobRepository repository) {
      this.repository = repository;
      currentMonth = repository.find(CurrentMonth.KEY);
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
      Glob absolutFirstTransaction = absoluteFirstTransactionForMonth.get(accountId);
      if (absolutFirstTransaction == null ||
          (TransactionComparator.ASCENDING_BANK.compare(transaction, absolutFirstTransaction) < 0)) {
        absoluteFirstTransactionForMonth.put(accountId, transaction);
      }

      SavingsData data = getOrCreateStat(transaction.get(Transaction.MONTH), accountId);

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
      if (!transaction.get(Transaction.PLANNED)
          && transaction.get(Transaction.BANK_MONTH).equals(currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH))
          && transaction.get(Transaction.BANK_DAY) <= currentMonth.get(CurrentMonth.LAST_TRANSACTION_DAY)
          && (lastRealKnownTransaction.get(accountId) == null ||
              TransactionComparator.ASCENDING_BANK.compare(transaction, lastRealKnownTransaction.get(accountId)) > 0)) {
        lastRealKnownTransaction.put(accountId, transaction);
      }
    }

    private SavingsData getOrCreateStat(Integer monthId, Integer accountId) {
      SavingsData data = this.computedData.get(monthId, accountId);
      if (data == null) {
        data = new SavingsData();
        this.computedData.put(monthId, accountId, data);
      }
      return data;
    }

    void complete() {
      GlobList months = repository.getAll(Month.TYPE).sort(Month.ID);
      for (Map.Entry<Integer, List<Integer>> seriesToAccounts : accountBySeries.entries()) {
        ReadOnlyGlobRepository.MultiFieldIndexed index =
          repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesToAccounts.getKey());
        for (Glob month : months) {
          Glob seriesBudget = index.findByIndex(SeriesBudget.MONTH, month.get(Month.ID)).getGlobs().getFirst();
          for (Integer accountId : seriesToAccounts.getValue()) {
            Boolean isSavings = isSavingsSeriesAndAccount.get(seriesToAccounts.getKey(), accountId);
            SavingsData data = getOrCreateStat(month.get(Month.ID), accountId);
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
      Map<Integer, Glob> lastTransaction = new HashMap<Integer, Glob>();
      for (Glob month : months) {
        Integer monthId = month.get(Month.ID);
        for (Map.Entry<Integer, SavingsData> entry : computedData.get(monthId).entrySet()) {
          Integer accountId = entry.getKey();

          Glob beginOfMonthTransaction = firstTransactionForMonth.get(accountId, monthId);
          Glob endOfMonthTransaction = lastTransactionForMonth.get(accountId, monthId);
          Double beginOfMonthPosition = null;
          Double endOfMonthPosition = null;

          if (beginOfMonthTransaction == null) { // donc endOfMonthTransaction == null
            beginOfMonthTransaction = lastTransaction.get(accountId);
            endOfMonthTransaction = beginOfMonthTransaction;
            if (beginOfMonthTransaction == null) {
              beginOfMonthTransaction = absoluteFirstTransactionForMonth.get(accountId);
              endOfMonthTransaction = beginOfMonthTransaction;
              if (beginOfMonthTransaction == null) {
                beginOfMonthPosition = repository.get(Key.create(Account.TYPE, accountId)).get(Account.FIRST_POSITION);
                endOfMonthPosition = beginOfMonthPosition;
              }
            }
          }

          if (endOfMonthTransaction != null) {
            lastTransaction.put(accountId, endOfMonthTransaction);
          }

          Double balance = null;
          if (beginOfMonthPosition != null) {
            balance = null;
          }
          else if (beginOfMonthTransaction != null && endOfMonthTransaction != null) {
            endOfMonthPosition = endOfMonthTransaction.get(Transaction.ACCOUNT_POSITION);
            beginOfMonthPosition = beginOfMonthTransaction.get(Transaction.ACCOUNT_POSITION);
            if (beginOfMonthPosition == null || endOfMonthPosition == null) {
              continue;
            }
            if (beginOfMonthTransaction.get(Transaction.BANK_MONTH) >= monthId) {
              beginOfMonthPosition = beginOfMonthPosition - beginOfMonthTransaction.get(Transaction.AMOUNT);
            }
            if (endOfMonthTransaction.get(Transaction.BANK_MONTH) > monthId) {
              endOfMonthPosition = endOfMonthPosition - endOfMonthTransaction.get(Transaction.AMOUNT);
            }
            balance = endOfMonthPosition - beginOfMonthPosition;
          }

          Key key = Key.create(SavingsBudgetStat.MONTH, monthId, SavingsBudgetStat.ACCOUNT, accountId);
          repository.create(key,
                            FieldValue.value(SavingsBudgetStat.BALANCE, balance),
                            FieldValue.value(SavingsBudgetStat.OUT, entry.getValue().out),
                            FieldValue.value(SavingsBudgetStat.OUT_PLANNED, entry.getValue().outPlanned),
                            FieldValue.value(SavingsBudgetStat.OUT_REMAINING, entry.getValue().outRemaining),
                            FieldValue.value(SavingsBudgetStat.SAVINGS, entry.getValue().savings),
                            FieldValue.value(SavingsBudgetStat.SAVINGS_PLANNED, entry.getValue().savingsPlanned),
                            FieldValue.value(SavingsBudgetStat.SAVINGS_REMAINING, entry.getValue().savingsRemaining),
                            value(SavingsBudgetStat.BEGIN_OF_MONTH_POSITION, beginOfMonthPosition),
                            value(SavingsBudgetStat.END_OF_MONTH_POSITION, endOfMonthPosition));
          if (lastRealKnownTransaction.get(accountId) != null) {
            Integer currentMonthId = lastRealKnownTransaction.get(accountId).get(Transaction.BANK_MONTH);
            if (currentMonthId.equals(monthId)) {
              repository.update(key,
                                value(SavingsBudgetStat.LAST_KNOWN_ACCOUNT_POSITION,
                                      lastRealKnownTransaction.get(accountId).get(Transaction.ACCOUNT_POSITION)),
                                value(SavingsBudgetStat.LAST_KNOWN_POSITION_DAY,
                                      lastRealKnownTransaction.get(accountId).get(Transaction.BANK_DAY)));
            }
          }
        }
      }
    }
  }
}
