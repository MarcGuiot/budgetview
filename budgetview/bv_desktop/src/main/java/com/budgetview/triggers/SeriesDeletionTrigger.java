package com.budgetview.triggers;

import com.budgetview.desktop.model.SeriesStat;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesBudget;
import com.budgetview.model.SubSeries;
import com.budgetview.model.Transaction;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.model.utils.GlobFunctor;

import static com.budgetview.desktop.model.SeriesStat.linkedToSeries;
import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class SeriesDeletionTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
      public void visitDeletion(Key seriesKey, FieldValues values) throws Exception {
        propagateSeriesDeletion(seriesKey, true, repository);
      }
    });
  }

  public static void propagateSeriesDeletion(Key seriesKey, boolean deleteMirror, GlobRepository repository) {
    repository.startChangeSet();
    try {
      Integer seriesId = seriesKey.get(Series.ID);

      if (deleteMirror) {
        for (Glob mirror : repository.getAll(Series.TYPE, fieldEquals(Series.MIRROR_SERIES, seriesId))) {
          if (mirror.exists()) {
            propagateSeriesDeletion(mirror.getKey(), false, repository);
          }
        }
      }

      repository.delete(SeriesStat.TYPE, linkedToSeries(seriesKey));

      repository.delete(repository.findByIndex(SeriesBudget.SERIES_INDEX,
                                               SeriesBudget.SERIES, seriesId).getGlobs());

      repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
        .getGlobs()
        .safeApply(new GlobFunctor() {
          public void run(Glob transaction, GlobRepository repository) throws Exception {
            repository.update(transaction.getKey(),
                              value(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                              value(Transaction.SUB_SERIES, null));
          }
        }, repository);

      repository.delete(SubSeries.TYPE,
                        linkedTo(seriesKey, SubSeries.SERIES));

    }
    finally {
      repository.completeChangeSet();
    }
  }
}
