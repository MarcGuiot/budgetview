package org.designup.picsou.triggers;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.Set;

public class SavingSeriesMirrorTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Integer savingSeries = repository.get(key).get(Series.SAVINGS_SERIES);
        if (values.contains(Series.SAVINGS_SERIES)) {
          if (savingSeries == null) {
            Integer previousSeries = values.getPrevious(Series.SAVINGS_SERIES);
            if (previousSeries != null) {
              Key pendingSeriesKey = Key.create(Series.TYPE, previousSeries);
              if (repository.get(pendingSeriesKey).get(Series.SAVINGS_SERIES).equals(previousSeries)) {
                repository.update(pendingSeriesKey, Series.SAVINGS_SERIES, null);
              }
            }
          }
        }
        else {
          if (savingSeries != null) {
            final Key pendingSeriesKey = Key.create(Series.TYPE, savingSeries);
            values.apply(new FieldValuesWithPrevious.Functor() {
              public void process(Field field, Object value, Object previousValue) throws Exception {
                if (field != Series.ID && field != Series.SAVINGS_SERIES && field != Series.SAVINGS_ACCOUNT) {
                  repository.update(pendingSeriesKey, field, value);
                }
              }
            });
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer previousSeries = previousValues.get(Series.SAVINGS_SERIES);
        if (previousSeries != null) {
          Key pendingSeriesKey = Key.create(Series.TYPE, previousSeries);
          if (repository.get(pendingSeriesKey).get(Series.SAVINGS_SERIES).equals(previousSeries)) {
            repository.update(pendingSeriesKey, Series.SAVINGS_SERIES, null);
          }
        }
      }
    });

    changeSet.safeVisit(SeriesBudget.TYPE, new DefaultChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        updatePendingSeriesBudget(key, repository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        updatePendingSeriesBudget(key, repository);
      }
    });

  }

  private void updatePendingSeriesBudget(Key key, GlobRepository repository) {
    Glob seriesBudget = repository.get(key);
    Glob series = repository.findLinkTarget(seriesBudget, SeriesBudget.SERIES);
    Integer pendingSeries = series.get(Series.SAVINGS_SERIES);
    if (pendingSeries != null) {
      Glob pendingBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, pendingSeries)
        .findByIndex(SeriesBudget.MONTH, seriesBudget.get(SeriesBudget.MONTH)).getGlobs().getFirst();
      Double amount = seriesBudget.get(SeriesBudget.AMOUNT);
      repository.update(pendingBudget.getKey(), SeriesBudget.AMOUNT,
                        amount != null ? -amount : null);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
