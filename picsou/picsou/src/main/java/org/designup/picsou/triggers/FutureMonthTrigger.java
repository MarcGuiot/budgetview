package org.designup.picsou.triggers;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.utils.directory.Directory;

import java.util.Set;
import java.util.SortedSet;

public class FutureMonthTrigger implements ChangeSetListener {
  private TimeService time;

  public FutureMonthTrigger(Directory directory) {
    time = directory.get(TimeService.class);
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(UserPreferences.TYPE)) {
      changeSet.safeVisit(UserPreferences.TYPE, new DefaultChangeSetVisitor() {
        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(UserPreferences.FUTURE_MONTH_COUNT)) {
            updateFuturMonth(repository, values.get(UserPreferences.FUTURE_MONTH_COUNT));
          }
        }

        public void visitCreation(Key key, FieldValues values) throws Exception {
          if (values.contains(UserPreferences.FUTURE_MONTH_COUNT)) {
            updateFuturMonth(repository, values.get(UserPreferences.FUTURE_MONTH_COUNT));
          }
        }
      });
    }
  }

  public void updateFuturMonth(GlobRepository repository, Integer monthCount) {
    int currentMonth = time.getCurrentMonthId();
    int[] futureMonth = Month.createMonthsWithFirst(currentMonth, monthCount);
    for (int month : futureMonth) {
      repository.findOrCreate(Key.create(Month.TYPE, month));
    }
    SortedSet<Integer> months = repository.getAll(Month.TYPE).getSortedSet(Month.ID);
    for (Integer month : months) {
      if (month < currentMonth) {
        continue;
      }
      if (month > currentMonth) {
        monthCount--;
        if (monthCount < 0) {
          repository.delete(Key.create(Month.TYPE, month));
        }
      }
    }
  }


  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(UserPreferences.TYPE)) {
      Glob userPreferences = repository.get(Key.create(UserPreferences.TYPE, UserPreferences.SINGLETON_ID));
      updateFuturMonth(repository, userPreferences.get(UserPreferences.FUTURE_MONTH_COUNT));
    }
  }
}

