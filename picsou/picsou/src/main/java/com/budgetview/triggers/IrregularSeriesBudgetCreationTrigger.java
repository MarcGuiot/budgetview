package com.budgetview.triggers;

import com.budgetview.model.Month;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesBudget;
import org.globsframework.model.*;

import static org.globsframework.model.FieldValue.value;

public class IrregularSeriesBudgetCreationTrigger extends AbstractChangeSetListener{
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        if (!key.get(Series.ID).equals(Series.UNCATEGORIZED_SERIES_ID)) {
          return;
        }
        ReadOnlyGlobRepository.MultiFieldIndexed index =
          repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, values.get(Series.ID));
        GlobList months = repository.getAll(Month.TYPE);
        for (Glob month : months) {
          createSeriesBudget(values, repository, month.get(Month.ID), index);
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });

  }

  private void createSeriesBudget(FieldValues values, GlobRepository repository, Integer monthId, ReadOnlyGlobRepository.MultiFieldIndexed index) {
    if (index.findByIndex(SeriesBudget.MONTH, monthId).getGlobs().isEmpty()) {
      repository.create(SeriesBudget.TYPE,
                        value(SeriesBudget.PLANNED_AMOUNT, 0.00),
                        value(SeriesBudget.MONTH, monthId),
                        value(SeriesBudget.DAY, Month.getDay(null, monthId)),
                        value(SeriesBudget.ACTIVE, true),
                        value(SeriesBudget.SERIES, values.get(Series.ID)));
    }
  }
}
