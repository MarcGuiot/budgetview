package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.Utils;

import java.util.Set;

public class SeriesStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    processSeriesBudget(changeSet, repository);
    processTransactions(changeSet, repository);
  }

  private void processSeriesBudget(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Key seriesStat = createKey(values.get(SeriesBudget.SERIES),
                                   values.get(SeriesBudget.MONTH));
        repository.findOrCreate(seriesStat);
        repository.update(seriesStat, SeriesStat.PLANNED_AMOUNT, values.get(SeriesBudget.AMOUNT));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob series = repository.get(key);
        if (values.contains(SeriesBudget.AMOUNT)) {
          Key seriesStat = createKey(series.get(SeriesBudget.SERIES),
                                     series.get(SeriesBudget.MONTH));
          repository.findOrCreate(seriesStat);
          repository.update(seriesStat, SeriesStat.PLANNED_AMOUNT,
                            values.get(SeriesBudget.AMOUNT));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Key seriesStat = createKey(previousValues.get(SeriesBudget.SERIES),
                                   previousValues.get(SeriesBudget.MONTH));
        Glob glob = repository.find(seriesStat);
        if (glob != null) {
          repository.delete(seriesStat);
        }
      }
    });
  }

  private void processTransactions(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        processTransaction(values, 1, repository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (!values.contains(Transaction.SERIES)
            && !values.contains(Transaction.MONTH)
            && !values.contains(Transaction.AMOUNT)) {
          return;
        }

        Glob transaction = repository.get(key);
        if (Utils.equal(transaction.get(Transaction.TRANSACTION_TYPE), TransactionType.PLANNED.getId())) {
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
        if (values.contains(Transaction.MONTH)) {
          previousMonthId = values.getPrevious(Transaction.MONTH);
          currentMonthId = values.get(Transaction.MONTH);
        }
        else {
          previousMonthId = transaction.get(Transaction.MONTH);
          currentMonthId = transaction.get(Transaction.MONTH);
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

        Glob previousStat = repository.find(createKey(previousSeriesId, previousMonthId));
        if (previousStat != null) {
          updateStat(previousStat, -1 * previousAmount, repository);
        }

        Glob currentStat = repository.find(createKey(currentSeriesId, currentMonthId));
        if (currentStat == null) {
          String name = repository.get(Key.create(Series.TYPE, currentSeriesId)).get(Series.NAME);
          throw new RuntimeException("SeriesStatTrigger.visitUpdate seires : " + name + " : " + currentSeriesId +
                                     " month = " + currentMonthId);
        }
        updateStat(currentStat, currentAmount, repository);
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        processTransaction(previousValues, -1, repository);
      }
    });
  }

  private void processTransaction(FieldValues values, int multiplier, GlobRepository repository) {
    final Integer seriesId = values.get(Transaction.SERIES);
    if (seriesId == null || Utils.equal(TransactionType.PLANNED.getId(), values.get(Transaction.TRANSACTION_TYPE))) {
      return;
    }

    Glob stat = repository.find(createKey(seriesId, values.get(Transaction.MONTH)));
    if (stat != null) {
      final Double transactionAmount = values.get(Transaction.AMOUNT);
      updateStat(stat, multiplier * transactionAmount, repository);
    }
  }

  private void updateStat(Glob stat, Double transactionAmount, GlobRepository repository) {
    repository.update(stat.getKey(), SeriesStat.AMOUNT,
                      stat.get(SeriesStat.AMOUNT) + transactionAmount);
  }

  public void init(GlobRepository repository) {
    repository.enterBulkDispatchingMode();
    try {
      repository.deleteAll(SeriesStat.TYPE);

      for (Glob month : repository.getAll(Month.TYPE)) {
        for (Glob series : repository.getAll(Series.TYPE)) {
          repository.create(createKey(series.get(Series.ID), month.get(Month.ID)));
        }
      }

      for (Glob transaction : repository.getAll(Transaction.TYPE)) {
        processTransaction(transaction, 1, repository);
      }

      GlobList seriesBudgets = repository.getAll(SeriesBudget.TYPE);
      for (Glob seriesBudget : seriesBudgets) {
        Key seriesStat = Key.create(SeriesStat.SERIES, seriesBudget.get(SeriesBudget.SERIES),
                                    SeriesStat.MONTH, seriesBudget.get(SeriesBudget.MONTH));
        repository.findOrCreate(seriesStat);
        repository.update(seriesStat, SeriesStat.PLANNED_AMOUNT, seriesBudget.get(SeriesBudget.AMOUNT));
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    init(repository);
  }

  private Key createKey(Integer seriesId, Integer monthId) {
    return Key.create(SeriesStat.SERIES, seriesId,
                      SeriesStat.MONTH, monthId);
  }
}
