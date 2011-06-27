package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.SubSeries;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;

public class SeriesDeletionTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
      public void visitDeletion(Key seriesKey, FieldValues values) throws Exception {
        propagateSeriesDeletion(seriesKey, repository);
      }
    });
  }

  static public void propagateSeriesDeletion(Key seriesKey, GlobRepository repository) {
    repository.startChangeSet();
    try {
      Integer seriesId = seriesKey.get(Series.ID);
      repository.delete(SeriesStat.TYPE,
                        GlobMatchers.linkedTo(seriesKey, SeriesStat.SERIES));

      repository.delete(repository.findByIndex(SeriesBudget.SERIES_INDEX,
                                               SeriesBudget.SERIES, seriesId).getGlobs());

//      repository.delete(Transaction.TYPE,
//                        GlobMatchers.and(
//                          GlobMatchers.fieldEquals(Transaction.SERIES, seriesId),
//                          GlobMatchers.fieldEquals(Transaction.CREATED_BY_SERIES, Boolean.TRUE)));

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
                        GlobMatchers.linkedTo(seriesKey, SubSeries.SERIES));
    }
    finally {
      repository.completeChangeSet();
    }
  }
}
