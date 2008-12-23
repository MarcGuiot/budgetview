package org.designup.picsou.triggers;

import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.*;
import org.designup.picsou.model.Series;
import org.designup.picsou.gui.model.SeriesStat;

public class SeriesDeletionTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
      public void visitDeletion(Key seriesKey, FieldValues values) throws Exception {
        GlobList stats = repository.getAll(SeriesStat.TYPE, GlobMatchers.linkedTo(seriesKey, SeriesStat.SERIES));
        repository.delete(stats);
      }
    });
  }
}
