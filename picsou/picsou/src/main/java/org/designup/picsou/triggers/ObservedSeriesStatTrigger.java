package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobPrinter;
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
          GlobList seriesBudgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, currentSeriesId)
            .findByIndex(SeriesBudget.MONTH, currentMonthId).getGlobs();
          Glob series = repository.get(Key.create(Series.TYPE, currentSeriesId));
          String name = series.get(Series.NAME);
          throw new RuntimeException("visitUpdate series : " + name + ", " + currentSeriesId +
                                     " month = " + currentMonthId + " series Budget :" +
                                     (seriesBudgets.isEmpty() ? " <none> " : seriesBudgets.get(0).get(SeriesBudget.AMOUNT)));
        }
        updateStat(currentStat, currentAmount, repository);
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        processTransaction(previousValues, -1, repository, false);
      }
    });
  }

  private void processTransaction(FieldValues values, int multiplier, GlobRepository repository, boolean throwIfNull) {
    final Integer seriesId = values.get(Transaction.SERIES);
    if (seriesId == null || Utils.equal(TransactionType.PLANNED.getId(), values.get(Transaction.TRANSACTION_TYPE))) {
      return;
    }

    Glob stat = repository.findOrCreate(createKey(seriesId, values.get(Transaction.MONTH)));
    if (stat != null) {
      final Double transactionAmount = values.get(Transaction.AMOUNT);
      updateStat(stat, multiplier * transactionAmount, repository);
    }
    else {
      if (throwIfNull) {
        throw new RuntimeException("Missing stat for month " + values.get(Transaction.MONTH) + " on series : " +
                                   GlobPrinter.toString(repository.get(Key.create(Series.TYPE, seriesId))));
      }
    }
  }

  private void updateStat(Glob stat, Double transactionAmount, GlobRepository repository) {
    repository.update(stat.getKey(), SeriesStat.AMOUNT,
                      stat.get(SeriesStat.AMOUNT) + transactionAmount);
  }

  public void init(GlobRepository repository) {
    repository.deleteAll(SeriesStat.TYPE);

    for (Glob month : repository.getAll(Month.TYPE)) {
      for (Glob series : repository.getAll(Series.TYPE)) {
        repository.create(createKey(series.get(Series.ID), month.get(Month.ID)));
      }
    }

    for (Glob transaction : repository.getAll(Transaction.TYPE)) {
      processTransaction(transaction, 1, repository, true);
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