package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.OnboardingStats;
import com.budgetview.analytics.utils.AnalyticsUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobFunctor;

import static org.globsframework.model.FieldValue.value;

public class OnboardingStatFunctor implements GlobFunctor {
  public void run(Glob usageCount, GlobRepository repository) throws Exception {

    Glob onboardingStats = repository.findOrCreate(Key.create(OnboardingStats.TYPE, usageCount.get(OnboardingStats.ID)));

    int firstTry = usageCount.get(OnboardingStats.FIRST_TRY_COUNT);
    int completedOnFirstTry = usageCount.get(OnboardingStats.ONBOARDING_COMPLETED_ON_FIRST_TRY);
    int importStartedOnFirstTry = usageCount.get(OnboardingStats.IMPORT_STARTED_ON_FIRST_TRY);
    int categorizationStartedOnFirstTry = usageCount.get(OnboardingStats.CATEGORIZATION_STARTED_ON_FIRST_TRY);
    int categorizationFinishedOnFirstTry = usageCount.get(OnboardingStats.CATEGORIZATION_FINISHED_ON_FIRST_TRY);

    repository.update(onboardingStats.getKey(),

                      value(OnboardingStats.FIRST_TRY_COMPLETION_RATIO,
                            AnalyticsUtils.ratio(completedOnFirstTry, firstTry)),

                      value(OnboardingStats.BOUNCE_BEFORE_IMPORT_RATIO,
                            AnalyticsUtils.ratio(firstTry - importStartedOnFirstTry, firstTry)),

                      value(OnboardingStats.COMPLETE_IMPORT_RATIO,
                            AnalyticsUtils.ratio(categorizationStartedOnFirstTry, firstTry)),

                      value(OnboardingStats.COMPLETE_CATEGORIZATION_RATIO,
                            AnalyticsUtils.ratio(categorizationFinishedOnFirstTry, firstTry))

    );
  }
}
