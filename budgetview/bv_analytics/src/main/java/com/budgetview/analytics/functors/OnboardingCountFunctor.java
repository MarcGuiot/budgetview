package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.OnboardingInfoEntry;
import com.budgetview.analytics.model.OnboardingStats;
import com.budgetview.analytics.utils.AnalyticsUtils;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;

import java.util.Date;

public class OnboardingCountFunctor implements GlobFunctor {
  public void run(Glob onboardingInfoEntry, GlobRepository repository) throws Exception {

    int count = onboardingInfoEntry.get(OnboardingInfoEntry.COUNT);
    boolean firstTry = count == 1;

    boolean initialStepsCompleted = onboardingInfoEntry.get(OnboardingInfoEntry.INITIAL_STEPS_COMPLETED, false);
    boolean importStarted = onboardingInfoEntry.get(OnboardingInfoEntry.IMPORT_STARTED, false);
    boolean categorizationSelectionDone = onboardingInfoEntry.get(OnboardingInfoEntry.CATEGORIZATION_SELECTION_DONE, false);
    boolean firstCategorizationDone = onboardingInfoEntry.get(OnboardingInfoEntry.FIRST_CATEGORIZATION_DONE, false);
    boolean categorizationSkipped = onboardingInfoEntry.get(OnboardingInfoEntry.CATEGORIZATION_SKIPPED, false);
    boolean gotoBudgetShown = onboardingInfoEntry.get(OnboardingInfoEntry.GOTO_BUDGET_SHOWN, false);

    count(OnboardingStats.FIRST_TRY_COUNT, firstTry,
          repository, onboardingInfoEntry);
    count(OnboardingStats.IMPORT_STARTED_ON_FIRST_TRY, firstTry && importStarted,
          repository, onboardingInfoEntry);
    count(OnboardingStats.CATEGORIZATION_STARTED_ON_FIRST_TRY, firstTry && categorizationSelectionDone,
          repository, onboardingInfoEntry);
    count(OnboardingStats.CATEGORIZATION_FINISHED_ON_FIRST_TRY, firstTry && (categorizationSkipped || gotoBudgetShown),
          repository, onboardingInfoEntry);
    count(OnboardingStats.ONBOARDING_COMPLETED_ON_FIRST_TRY, firstTry && initialStepsCompleted,
          repository, onboardingInfoEntry);
  }

  private void count(IntegerField field, boolean condition, GlobRepository repository, Glob userProgress) {
    if (!condition) {
      return;
    }

    Date date = userProgress.get(OnboardingInfoEntry.DATE);
    Glob onboardingCount = AnalyticsUtils.getWeekStat(date, OnboardingStats.TYPE,
                                                     OnboardingStats.ID, OnboardingStats.LAST_DAY,
                                                     repository);

    repository.update(onboardingCount.getKey(), field, onboardingCount.get(field) + 1);
  }
}
