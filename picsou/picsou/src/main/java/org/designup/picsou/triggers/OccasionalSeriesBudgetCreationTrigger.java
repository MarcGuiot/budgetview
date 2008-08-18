package org.designup.picsou.triggers;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Calendar;
import java.util.Set;

public class OccasionalSeriesBudgetCreationTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Key.create(Series.TYPE, Series.OCCASIONAL_SERIES_ID), new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        GlobList months = repository.getAll(Month.TYPE);
        for (Glob month : months) {
          repository.create(SeriesBudget.TYPE,
                            FieldValue.value(SeriesBudget.AMOUNT, 0.),
                            FieldValue.value(SeriesBudget.MONTH, month.get(Month.ID)),
                            FieldValue.value(SeriesBudget.DAY, Month.getDay(null, month.get(Month.ID), Calendar.getInstance())),
                            FieldValue.value(SeriesBudget.ACTIVE, true),
                            FieldValue.value(SeriesBudget.SERIES, Series.OCCASIONAL_SERIES_ID));

        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
    changeSet.safeVisit(Month.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        System.out.println("OccasionalSeriesBudgetCreationTrigger.visitCreation " + values.get(Month.ID));
        if (repository.find(Key.create(Series.TYPE, Series.OCCASIONAL_SERIES_ID)) != null) {
          repository.create(SeriesBudget.TYPE,
                            FieldValue.value(SeriesBudget.AMOUNT, 0.),
                            FieldValue.value(SeriesBudget.MONTH, values.get(Month.ID)),
                            FieldValue.value(SeriesBudget.DAY, Month.getDay(null, values.get(Month.ID), Calendar.getInstance())),
                            FieldValue.value(SeriesBudget.ACTIVE, true),
                            FieldValue.value(SeriesBudget.SERIES, Series.OCCASIONAL_SERIES_ID));
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        GlobList seriesBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, Series.OCCASIONAL_SERIES_ID)
          .findByIndex(SeriesBudget.MONTH, previousValues.get(Month.ID)).getGlobs();
        repository.delete(seriesBudget);
      }
    });

  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
