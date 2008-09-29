package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;

public class PlannedSeriesStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    processSeriesBudget(changeSet, repository);
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
        if (values.contains(SeriesBudget.AMOUNT)) {
          Glob seriesBudget = repository.get(key);
          Key seriesStat = createKey(seriesBudget.get(SeriesBudget.SERIES),
                                     seriesBudget.get(SeriesBudget.MONTH));
          repository.findOrCreate(seriesStat);
          repository.update(seriesStat, SeriesStat.PLANNED_AMOUNT, seriesBudget.get(SeriesBudget.AMOUNT));
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

  public void init(GlobRepository repository) {

    for (Glob month : repository.getAll(Month.TYPE)) {
      for (Glob series : repository.getAll(Series.TYPE)) {
        repository.findOrCreate(createKey(series.get(Series.ID), month.get(Month.ID)));
      }
    }
    GlobList seriesBudgets = repository.getAll(SeriesBudget.TYPE);
    for (Glob seriesBudget : seriesBudgets) {
      Key seriesStat = createKey(seriesBudget.get(SeriesBudget.SERIES),
                                 seriesBudget.get(SeriesBudget.MONTH));
      repository.findOrCreate(seriesStat);
      repository.update(seriesStat, SeriesStat.PLANNED_AMOUNT, seriesBudget.get(SeriesBudget.AMOUNT));
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