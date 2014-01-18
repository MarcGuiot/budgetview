package org.designup.picsou.triggers;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesGroup;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class DeleteUnusedSeriesGroupTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Series.GROUP)) {
          deletePreviousGroupIfNeeded(values.getPrevious(Series.GROUP), repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        deletePreviousGroupIfNeeded(previousValues.get(Series.GROUP), repository);
      }
    });
  }

  private void deletePreviousGroupIfNeeded(Integer previousGroupId, GlobRepository repository) {
    if ((previousGroupId != null) &&
        !repository.contains(Series.TYPE, fieldEquals(Series.GROUP, previousGroupId))) {
      repository.delete(Key.create(SeriesGroup.TYPE, previousGroupId));
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Series.TYPE) || changedTypes.contains(SeriesGroup.TYPE)) {
      for (Glob group : repository.getAll(SeriesGroup.TYPE)) {
        deletePreviousGroupIfNeeded(group.get(SeriesGroup.ID), repository);
      }
    }
  }
}
