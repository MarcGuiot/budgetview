package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.WeekUsageCount;
import com.budgetview.analytics.model.WeekUsageStat;
import com.budgetview.analytics.utils.AnalyticsUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobFunctor;

import static org.globsframework.model.FieldValue.value;

public class UserProgressStatFunctor implements GlobFunctor {
  public void run(Glob usageCount, GlobRepository repository) throws Exception {

    Glob usageStat = repository.findOrCreate(Key.create(WeekUsageStat.TYPE, usageCount.get(WeekUsageCount.ID)));

    int firstTry = usageCount.get(WeekUsageCount.FIRST_TRY_COUNT);
    int importStartedOnFirstTry = usageCount.get(WeekUsageCount.IMPORT_STARTED_ON_FIRST_TRY);
    int categorizationStartedOnFirstTry = usageCount.get(WeekUsageCount.CATEGORIZATION_STARTED_ON_FIRST_TRY);
    int categorizationFinishedOnFirstTry = usageCount.get(WeekUsageCount.CATEGORIZATION_FINISHED_ON_FIRST_TRY);
    int completedOnFirstTry = usageCount.get(WeekUsageCount.COMPLETED_ON_FIRST_TRY);
    int lostOnFirstTry = firstTry - completedOnFirstTry;

    int secondTry = usageCount.get(WeekUsageCount.SECOND_TRY_COUNT);
    int importStartedOnSecondTry = usageCount.get(WeekUsageCount.IMPORT_STARTED_ON_SECOND_TRY);
    int categorizationStartedOnSecondTry = usageCount.get(WeekUsageCount.CATEGORIZATION_STARTED_ON_SECOND_TRY);
    int categorizationFinishedOnSecondTry = usageCount.get(WeekUsageCount.CATEGORIZATION_FINISHED_ON_SECOND_TRY);
    int completedOnSecondTry = usageCount.get(WeekUsageCount.COMPLETED_ON_SECOND_TRY);
    int lostOnSecondTry = secondTry - completedOnSecondTry;

    repository.update(usageStat.getKey(),

                      value(WeekUsageStat.LAST_DAY,
                            usageCount.get(WeekUsageCount.LAST_DAY)),

                      value(WeekUsageStat.FIRST_TRY_COUNT, firstTry),

                      value(WeekUsageStat.COMPLETION_RATE_ON_FIRST_TRY,
                            AnalyticsUtils.ratio(completedOnFirstTry, firstTry)),

                      value(WeekUsageStat.LOSS_BEFORE_FIRST_IMPORT,
                            AnalyticsUtils.lossRatio(firstTry, importStartedOnFirstTry, lostOnFirstTry)),

                      value(WeekUsageStat.LOSS_DURING_FIRST_IMPORT,
                            AnalyticsUtils.lossRatio(importStartedOnFirstTry, categorizationStartedOnFirstTry, lostOnFirstTry)),

                      value(WeekUsageStat.LOSS_DURING_FIRST_CATEGORIZATION,
                            AnalyticsUtils.lossRatio(categorizationStartedOnFirstTry, categorizationFinishedOnFirstTry, lostOnFirstTry)),

                      value(WeekUsageStat.LOSS_AFTER_FIRST_CATEGORIZATION,
                            AnalyticsUtils.lossRatio(categorizationFinishedOnFirstTry, completedOnFirstTry, lostOnFirstTry)),

                      value(WeekUsageStat.SECOND_TRY_COUNT, secondTry),

                      value(WeekUsageStat.COMPLETION_RATE_ON_SECOND_TRY,
                            AnalyticsUtils.ratio(completedOnSecondTry, secondTry)),

                      value(WeekUsageStat.LOSS_BEFORE_SECOND_IMPORT,
                            AnalyticsUtils.lossRatio(secondTry, importStartedOnSecondTry, lostOnSecondTry)),

                      value(WeekUsageStat.LOSS_DURING_SECOND_IMPORT,
                            AnalyticsUtils.lossRatio(importStartedOnSecondTry, categorizationStartedOnSecondTry, lostOnSecondTry)),

                      value(WeekUsageStat.LOSS_DURING_SECOND_CATEGORIZATION,
                            AnalyticsUtils.lossRatio(categorizationStartedOnSecondTry, categorizationFinishedOnSecondTry, lostOnSecondTry)),

                      value(WeekUsageStat.LOSS_AFTER_SECOND_CATEGORIZATION,
                            AnalyticsUtils.lossRatio(categorizationFinishedOnSecondTry, completedOnSecondTry, lostOnSecondTry))
    );

  }
}
