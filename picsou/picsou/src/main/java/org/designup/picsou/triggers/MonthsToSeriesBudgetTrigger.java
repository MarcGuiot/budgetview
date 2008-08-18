package org.designup.picsou.triggers;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Set;

public class MonthsToSeriesBudgetTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Month.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer monthId = values.get(Month.ID);
        GlobList seriesList = repository.getAll(Series.TYPE,
                                                GlobMatchers.or(
                                                  GlobMatchers.fieldIsNull(Series.LAST_MONTH),
                                                  GlobMatchers.fieldGreaterOrEqual(Series.LAST_MONTH, monthId)
                                                ));
        for (Glob series : seriesList) {
          if (series.get(Series.AMOUNT) != null) {
            repository.create(SeriesBudget.TYPE,
                              FieldValue.value(SeriesBudget.ACTIVE, series.get(Series.getField(monthId))),
                              FieldValue.value(SeriesBudget.SERIES, series.get(Series.ID)),
                              FieldValue.value(SeriesBudget.AMOUNT, series.get(Series.AMOUNT)),
                              FieldValue.value(SeriesBudget.DAY, series.get(Series.DAY)),
                              FieldValue.value(SeriesBudget.MONTH, monthId));
          }
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer deletedMonth = previousValues.get(Month.ID);
        repository.getAll(SeriesBudget.TYPE, GlobMatchers.fieldEquals(SeriesBudget.MONTH, deletedMonth));
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
