package com.budgetview.triggers;

import com.budgetview.desktop.model.SeriesStat;
import com.budgetview.model.Month;
import com.budgetview.model.Series;
import com.budgetview.model.Transaction;
import com.budgetview.shared.utils.Amounts;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.utils.Utils;

import java.util.Set;

public class ActualSeriesStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    processTransactions(changeSet, repository);
  }

  private void processTransactions(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key transactionKey, FieldValues values) throws Exception {
        processTransaction(values, 1, repository, true);
      }

      public void visitUpdate(Key transactionKey, FieldValuesWithPrevious values) throws Exception {
        if (!values.contains(Transaction.SERIES)
            && !values.contains(Transaction.BUDGET_MONTH)
            && !values.contains(Transaction.AMOUNT)
            && !values.contains(Transaction.PLANNED)) {
          return;
        }

        Glob transaction = repository.get(transactionKey);
        if (transaction.isTrue(Transaction.PLANNED) || Transaction.isOpenCloseAccount(transaction)) {
          return;
        }

        Integer previousSeriesId;
        Integer currentSeriesId;
        if (values.contains(Transaction.SERIES)) {
          previousSeriesId = values.getPrevious(Transaction.SERIES);
          currentSeriesId = values.get(Transaction.SERIES);
        }
        else {
          previousSeriesId = transaction.get(Transaction.SERIES);
          currentSeriesId = transaction.get(Transaction.SERIES);
        }

        Integer previousMonthId;
        Integer currentMonthId;
        if (values.contains(Transaction.BUDGET_MONTH)) {
          previousMonthId = values.getPrevious(Transaction.BUDGET_MONTH);
          currentMonthId = values.get(Transaction.BUDGET_MONTH);
        }
        else {
          previousMonthId = transaction.get(Transaction.BUDGET_MONTH);
          currentMonthId = transaction.get(Transaction.BUDGET_MONTH);
        }

        Boolean isPlanned = null;
        if (values.contains(Transaction.PLANNED)) {
          isPlanned = values.get(Transaction.PLANNED);
        }

        Double previousAmount;
        Double currentAmount;
        if (values.contains(Transaction.AMOUNT)) {
          previousAmount = values.getPrevious(Transaction.AMOUNT);
          currentAmount = values.get(Transaction.AMOUNT);
        }
        else {
          previousAmount = transaction.get(Transaction.AMOUNT);
          currentAmount = transaction.get(Transaction.AMOUNT);
        }

        Integer accountId = transaction.get(Transaction.ACCOUNT);

        Glob previousStat = repository.find(SeriesStat.createKeyForSeries(previousSeriesId, previousMonthId));
        Glob previousAccountStat = repository.find(SeriesStat.createKeyForSeries(accountId, previousSeriesId, previousMonthId));
        if (previousStat != null && (isPlanned == null || isPlanned)) {
          updateStat(previousStat, previousAmount, -1, repository);
          updateStat(previousAccountStat, previousAmount, -1, repository);
        }

        Glob currentStat = repository.findOrCreate(SeriesStat.createKeyForSeries(currentSeriesId, currentMonthId));
        Glob currentAccountStat = repository.findOrCreate(SeriesStat.createKeyForSeries(accountId, currentSeriesId, currentMonthId));
        if ((isPlanned == null || !isPlanned)) {
          updateStat(currentStat, currentAmount, 1, repository);
          updateStat(currentAccountStat, currentAmount, 1, repository);
        }
      }

      public void visitDeletion(Key transactionKey, FieldValues previousValues) throws Exception {
        processTransaction(previousValues, -1, repository, false);
      }
    });
  }

  private void processTransaction(FieldValues values, int multiplier, GlobRepository repository, boolean throwIfNull) {
    final Integer seriesId = values.get(Transaction.SERIES);
    if (seriesId == null ||
        values.isTrue(Transaction.PLANNED) ||
        Transaction.isOpenCloseAccount(values)) {
      return;
    }

    final Double transactionAmount = values.get(Transaction.AMOUNT);
    Glob stat = repository.findOrCreate(SeriesStat.createKeyForSeries(seriesId, values.get(Transaction.BUDGET_MONTH)));
    updateStat(stat, transactionAmount, multiplier, repository);
    Glob accountStat = repository.findOrCreate(SeriesStat.createKeyForSeries(values.get(Transaction.ACCOUNT), seriesId, values.get(Transaction.BUDGET_MONTH)));
    if (accountStat == null) System.out.println("ActualSeriesStatTrigger.processTransaction - accountStat is NULL");
    updateStat(accountStat, transactionAmount, multiplier, repository);
  }

  private void updateStat(Glob stat, Double transactionAmount, int multiplier, GlobRepository repository) {
    if (stat == null) {
      return;
    }
    double newValue = Utils.zeroIfNull(stat.get(SeriesStat.ACTUAL_AMOUNT)) + multiplier * transactionAmount;
    if (Amounts.isNearZero(newValue)) {
      IsTransactionPresent transactionPresent = new IsTransactionPresent(stat);
      repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, stat.get(SeriesStat.TARGET))
        .saveApply(transactionPresent, repository);
      if (transactionPresent.found) {
        repository.update(stat.getKey(), SeriesStat.ACTUAL_AMOUNT, 0.00);
      }
      else {
        repository.update(stat.getKey(), SeriesStat.ACTUAL_AMOUNT, null);
      }
    }
    else {
      repository.update(stat.getKey(), SeriesStat.ACTUAL_AMOUNT, newValue);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    init(repository);
  }

  public void init(GlobRepository repository) {
    repository.deleteAll(SeriesStat.TYPE);

    GlobList allSeries = repository.getAll(Series.TYPE);
    for (Glob month : repository.getAll(Month.TYPE)) {
      for (Glob series : allSeries) {
        repository.create(SeriesStat.createKeyForSeries(series.get(Series.ID), month.get(Month.ID)));
      }
    }

    for (Glob transaction : repository.getAll(Transaction.TYPE)) {
      processTransaction(transaction, 1, repository, true);
    }
  }

  private static class IsTransactionPresent implements GlobFunctor {
    private final int month;
    private boolean found = false;

    public IsTransactionPresent(Glob stat) {
      this.month = stat.get(SeriesStat.MONTH);
    }

    public void run(Glob glob, GlobRepository repository) throws Exception {
      if (glob.get(Transaction.BUDGET_MONTH) == month && !glob.isTrue(Transaction.PLANNED)) {
        found = true;
      }
    }
  }
}