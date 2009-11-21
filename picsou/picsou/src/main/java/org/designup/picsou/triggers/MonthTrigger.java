package org.designup.picsou.triggers;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

import java.util.List;
import java.util.Set;

public class MonthTrigger implements ChangeSetListener {
  private TimeService time;

  public MonthTrigger(Directory directory) {
    time = directory.get(TimeService.class);
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(UserPreferences.TYPE)) {
      changeSet.safeVisit(UserPreferences.TYPE, new DefaultChangeSetVisitor() {
        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(UserPreferences.FUTURE_MONTH_COUNT)) {
            updateMonth(repository, values.get(UserPreferences.FUTURE_MONTH_COUNT));
          }
        }

        public void visitCreation(Key key, FieldValues values) throws Exception {
          if (values.contains(UserPreferences.FUTURE_MONTH_COUNT)) {
            updateMonth(repository, values.get(UserPreferences.FUTURE_MONTH_COUNT));
          }
        }
      });
    }
    if (changeSet.containsChanges(CurrentMonth.KEY)) {
      updateMonth(repository, repository.get(UserPreferences.KEY).get(UserPreferences.FUTURE_MONTH_COUNT));
    }
  }

  public void updateMonth(GlobRepository repository, Integer monthCount) {
    try {
      repository.startChangeSet();
      Glob firstMonth = repository.getAll(Month.TYPE).getFirst();
      int startMonth = time.getCurrentMonthId();
      if (firstMonth != null) {
        startMonth = firstMonth.get(Month.ID);
      }
      int currentMonth = time.getCurrentMonthId();
      List<Integer> pastMonth = Month.createMonths(startMonth, currentMonth);
      for (Integer monthId : pastMonth) {
        repository.findOrCreate(Key.create(Month.TYPE, monthId));
      }
      int[] futureMonth = Month.createCountMonthsWithFirst(currentMonth, monthCount);
      for (int month : futureMonth) {
        repository.findOrCreate(Key.create(Month.TYPE, month));
      }
      Integer[] months = repository.getAll(Month.TYPE).getSortedArray(Month.ID);
      for (int i = months.length - 1; i >= 0; i--) {
        Integer month = months[i];
        if (Month.distance(currentMonth, month) > monthCount) {
          GlobList all = repository.getAll(Transaction.TYPE,
                                           and(fieldEquals(Transaction.MONTH, month),
                                               isFalse(Transaction.PLANNED)));
          if (!all.isEmpty()) {
            return;
          }
          repository.delete(Key.create(Month.TYPE, month));
        }
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(UserPreferences.TYPE)) {
      Glob userPreferences = repository.find(Key.create(UserPreferences.TYPE, UserPreferences.SINGLETON_ID));
      if (userPreferences != null) {
        updateMonth(repository, userPreferences.get(UserPreferences.FUTURE_MONTH_COUNT));
      }
    }
  }
}

