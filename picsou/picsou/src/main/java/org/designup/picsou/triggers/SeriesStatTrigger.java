package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.model.utils.GlobMatchers;

import java.util.List;

public class SeriesStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    processSeries(changeSet, repository);
    processMonths(changeSet, repository);
    processTransactions(changeSet, repository);
  }

  private void processSeries(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key seriesKey, FieldValues values) throws Exception {
        Integer seriesId = seriesKey.get(Series.ID);
        for (Glob month : repository.getAll(Month.TYPE)) {
          final Key statKey = createKey(seriesId, month.get(Month.ID));
          repository.findOrCreate(statKey);
          repository.update(statKey, SeriesStat.PLANNED_AMOUNT, values.get(Series.AMOUNT));
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (!values.contains(Series.AMOUNT)) {
          return;
        }
        for (Glob seriesStat : repository.getAll(SeriesStat.TYPE,
                                                 GlobMatchers.fieldEquals(SeriesStat.SERIES, key.get(Series.ID)))) {
          repository.update(seriesStat.getKey(), SeriesStat.PLANNED_AMOUNT, values.getValue(Series.AMOUNT));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        GlobList stats = repository.getAll(SeriesStat.TYPE,
                                           GlobMatchers.fieldEquals(SeriesStat.SERIES, key.get(Series.ID)));
        repository.delete(stats);
      }
    });
  }

  private void processMonths(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Month.TYPE, new DefaultChangeSetVisitor() {
      public void visitCreation(Key monthKey, FieldValues values) throws Exception {
        Integer monthId = monthKey.get(Month.ID);
        for (Glob series : repository.getAll(Series.TYPE)) {
          repository.findOrCreate(createKey(series.get(Series.ID), monthId));
        }
      }

      public void visitDeletion(Key monthKey, FieldValues values) throws Exception {
        final Integer monthId = monthKey.get(Month.ID);
        GlobList stats = repository.getAll(SeriesStat.TYPE,
                                           GlobMatchers.fieldEquals(SeriesStat.MONTH, monthId));
        repository.delete(stats);
      }
    });
  }

  private void processTransactions(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        processCreationOrDeletion(values, 1);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (!values.contains(Transaction.SERIES)
            && !values.contains(Transaction.MONTH)
            && !values.contains(Transaction.AMOUNT)) {
          return;
        }

        Glob transaction = repository.get(key);

        Integer previousSeriesId;
        Integer currentSeriesId;
        if (values.contains(Transaction.SERIES)) {
          previousSeriesId = values.getPrevious(Transaction.SERIES);
          currentSeriesId = values.get(Transaction.SERIES);
        }
        else {
          previousSeriesId = null;
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

        if (previousSeriesId != null) {
          Glob stat = repository.find(createKey(previousSeriesId, previousMonthId));
          if (stat != null) {
            update(stat, -1 * previousAmount, repository);
          }
        }

        if (currentSeriesId != null) {
          Glob stat = repository.find(createKey(currentSeriesId, currentMonthId));
          update(stat, currentAmount, repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        processCreationOrDeletion(previousValues, -1);
      }

      private void processCreationOrDeletion(FieldValues values, int multiplier) {
        final Integer seriesId = values.get(Transaction.SERIES);
        if (seriesId == null) {
          return;
        }

        Glob stat = repository.find(createKey(seriesId, values.get(Transaction.MONTH)));
        if (stat != null) {
          final Double transactionAmount = values.get(Transaction.AMOUNT);
          update(stat, multiplier * transactionAmount, repository);
        }
      }

      private void update(Glob stat, Double transactionAmount, GlobRepository repository) {
        repository.update(stat.getKey(), SeriesStat.AMOUNT,
                          stat.get(SeriesStat.AMOUNT) + transactionAmount);
      }
    });
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
    if (!changedTypes.contains(Transaction.TYPE)
        && !changedTypes.contains(Series.TYPE)
        && !changedTypes.contains(Month.TYPE)) {
      return;
    }
  }

  private Key createKey(Integer seriesId, Integer monthId) {
    return KeyBuilder.createFromValues(SeriesStat.TYPE,
                                       value(SeriesStat.MONTH, monthId),
                                       value(SeriesStat.SERIES, seriesId));
  }
}
