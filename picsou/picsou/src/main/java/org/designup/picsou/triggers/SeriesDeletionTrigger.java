package org.designup.picsou.triggers;

import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.*;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.SubSeries;
import org.designup.picsou.gui.model.SeriesStat;

public class SeriesDeletionTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
      public void visitDeletion(Key seriesKey, FieldValues values) throws Exception {
        Integer seriesId = seriesKey.get(Series.ID);

        repository.delete(repository.getAll(SeriesStat.TYPE,
                                            GlobMatchers.linkedTo(seriesKey, SeriesStat.SERIES)));

        repository.delete(repository.findByIndex(SeriesBudget.SERIES_INDEX, 
                                                 SeriesBudget.SERIES, seriesId).getGlobs());

        GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
          .getGlobs();
        transactions.apply(new GlobFunctor() {
            public void run(Glob glob, GlobRepository repository) throws Exception {
              repository.update(glob.getKey(), FieldValue.value(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                                FieldValue.value(Transaction.SUB_SERIES, null));
            }
          }, repository);

        repository.delete(repository.getAll(SubSeries.TYPE,
                                            GlobMatchers.linkedTo(seriesKey, SubSeries.SERIES)));
      }
    });
  }
}
