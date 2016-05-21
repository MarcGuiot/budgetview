package com.budgetview.triggers;

import com.budgetview.model.Day;
import com.budgetview.model.Month;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Set;

public class DayTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(Month.TYPE)) {
      changeSet.safeVisit(Month.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
          createDays(key, repository);
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
          repository.delete(Day.TYPE, GlobMatchers.fieldEquals(Day.MONTH, key.get(Month.ID)));
        }
      });
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Month.TYPE)) {
      repository.deleteAll(Day.TYPE);
      for (Glob month : repository.getAll(Month.TYPE)) {
        createDays(month.getKey(), repository);
      }
    }
  }

  private void createDays(Key monthKey, GlobRepository repository) {
    Integer month = monthKey.get(Month.ID);
    int maxDay = Month.getLastDayNumber(month);
    for (int day = 1; day <= maxDay; day++) {
      repository.findOrCreate(Key.create(Day.MONTH, month, Day.DAY, day));
    }
  }
}
