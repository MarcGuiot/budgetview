package org.designup.picsou.triggers;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.utils.Utils;

import java.util.Set;

public class ObservedSeriesStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    processTransactions(changeSet, repository);
  }

  private void processTransactions(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        processTransaction(values, 1, repository, true);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (!values.contains(Transaction.SERIES)
            && !values.contains(Transaction.BUDGET_MONTH)
            && !values.contains(Transaction.AMOUNT)
            && !values.contains(Transaction.PLANNED)) {
          return;
        }

        Glob transaction = repository.get(key);
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

        Glob previousStat = repository.find(SeriesStat.createKey(previousSeriesId, previousMonthId));
        if (previousStat != null && (isPlanned == null || isPlanned)) {
          updateStat(previousStat, previousAmount, -1, repository);
        }

        Glob currentStat = repository.findOrCreate(SeriesStat.createKey(currentSeriesId, currentMonthId));
        if ((isPlanned == null || !isPlanned)) {
          updateStat(currentStat, currentAmount, 1, repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        processTransaction(previousValues, -1, repository, false);
      }
    });
  }

  private void processTransaction(FieldValues values, int multiplier, GlobRepository repository, boolean throwIfNull) {
    final Integer seriesId = values.get(Transaction.SERIES);
    if (seriesId == null || values.isTrue(Transaction.PLANNED) || Transaction.isOpenCloseAccount(values)) {
      return;
    }

    Glob stat = repository.findOrCreate(SeriesStat.createKey(seriesId, values.get(Transaction.BUDGET_MONTH)));
    if (stat != null) {
      final Double transactionAmount = values.get(Transaction.AMOUNT);
      updateStat(stat, transactionAmount, multiplier, repository);
    }
    else {
      if (throwIfNull) {
        throw new RuntimeException("Missing stat for month " + values.get(Transaction.BUDGET_MONTH) + " on series : " +
                                   GlobPrinter.toString(repository.get(Key.create(Series.TYPE, seriesId))));
      }
    }
  }

  private void updateStat(Glob stat, Double transactionAmount, int multiplier, GlobRepository repository) {
    double newValue = Utils.zeroIfNull(stat.get(SeriesStat.AMOUNT)) + multiplier * transactionAmount;
    if (Amounts.isNearZero(newValue)) {
      IsTransactionPresent transactionPresent = new IsTransactionPresent(stat);
      repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, stat.get(SeriesStat.SERIES))
        .saveApply(transactionPresent, repository);
      if (transactionPresent.found) {
        repository.update(stat.getKey(), SeriesStat.AMOUNT, 0.);
      }
      else {
        repository.update(stat.getKey(), SeriesStat.AMOUNT, null);
      }
    }
    else {
      repository.update(stat.getKey(), SeriesStat.AMOUNT, newValue);
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
        repository.create(SeriesStat.createKey(series.get(Series.ID), month.get(Month.ID)));
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