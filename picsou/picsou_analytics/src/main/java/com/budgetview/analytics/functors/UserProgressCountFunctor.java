package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.UserProgressInfoEntry;
import com.budgetview.analytics.model.WeekUsageCount;
import com.budgetview.analytics.utils.AnalyticsUtils;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;

import java.util.Date;

public class UserProgressCountFunctor implements GlobFunctor {
  public void run(Glob userProgress, GlobRepository repository) throws Exception {

    int count = userProgress.get(UserProgressInfoEntry.COUNT);
    boolean firstTry = count == 1;
    boolean secondTry = count == 2;

    boolean initialStepsCompleted = userProgress.get(UserProgressInfoEntry.INITIAL_STEPS_COMPLETED, false);
    boolean importStarted = userProgress.get(UserProgressInfoEntry.IMPORT_STARTED, false);
    boolean categorizationSelectionDone = userProgress.get(UserProgressInfoEntry.CATEGORIZATION_SELECTION_DONE, false);
    boolean categorizationAreaSelectionDone = userProgress.get(UserProgressInfoEntry.CATEGORIZATION_AREA_SELECTION_DONE, false);
    boolean firstCategorizationDone = userProgress.get(UserProgressInfoEntry.FIRST_CATEGORIZATION_DONE, false);
    boolean categorizationSkipped = userProgress.get(UserProgressInfoEntry.CATEGORIZATION_SKIPPED, false);
    boolean gotoBudgetShown = userProgress.get(UserProgressInfoEntry.GOTO_BUDGET_SHOWN, false);

    count(WeekUsageCount.FIRST_TRY_COUNT, firstTry,
          repository, userProgress);
    count(WeekUsageCount.IMPORT_STARTED_ON_FIRST_TRY, firstTry && importStarted,
          repository, userProgress);
    count(WeekUsageCount.CATEGORIZATION_STARTED_ON_FIRST_TRY, firstTry && categorizationSelectionDone,
          repository, userProgress);
    count(WeekUsageCount.CATEGORIZATION_FINISHED_ON_FIRST_TRY, firstTry && (categorizationSkipped || gotoBudgetShown),
          repository, userProgress);
    count(WeekUsageCount.COMPLETED_ON_FIRST_TRY, firstTry && initialStepsCompleted,
          repository, userProgress);
    count(WeekUsageCount.SECOND_TRY_COUNT, secondTry,
          repository, userProgress);
    count(WeekUsageCount.IMPORT_STARTED_ON_SECOND_TRY, secondTry && importStarted,
          repository, userProgress);
    count(WeekUsageCount.CATEGORIZATION_STARTED_ON_SECOND_TRY, secondTry && categorizationSelectionDone,
          repository, userProgress);
    count(WeekUsageCount.CATEGORIZATION_FINISHED_ON_SECOND_TRY, secondTry && (categorizationSkipped || gotoBudgetShown),
          repository, userProgress);
    count(WeekUsageCount.COMPLETED_ON_SECOND_TRY, secondTry && initialStepsCompleted,
          repository, userProgress);
  }

  private void count(IntegerField field, boolean condition, GlobRepository repository, Glob userProgress) {
    if (!condition) {
      return;
    }

    Date date = userProgress.get(UserProgressInfoEntry.DATE);
    Glob weekUsageCount = AnalyticsUtils.getWeekStat(date, WeekUsageCount.TYPE,
                                                     WeekUsageCount.ID, WeekUsageCount.LAST_DAY,
                                                     repository);

    repository.update(weekUsageCount.getKey(), field, weekUsageCount.get(field) + 1);
  }
}
