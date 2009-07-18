package org.designup.picsou.triggers;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Calendar;
import java.util.Set;

public class IrregularSeriesBudgetCreationTrigger implements ChangeSetListener {
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
                        FieldValue.value(SeriesBudget.AMOUNT, 0.),
                        FieldValue.value(SeriesBudget.MONTH, monthId),
                        FieldValue.value(SeriesBudget.DAY, Month.getDay(null, monthId, Calendar.getInstance())),
                        FieldValue.value(SeriesBudget.ACTIVE, true),
                        FieldValue.value(SeriesBudget.SERIES, values.get(Series.ID)));
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
