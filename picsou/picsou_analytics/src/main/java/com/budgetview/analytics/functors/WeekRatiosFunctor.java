package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.WeekStats;
import com.budgetview.analytics.utils.AnalyticsUtils;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;

import static org.globsframework.model.FieldValue.value;

public class WeekRatiosFunctor implements GlobFunctor {

  public void run(Glob weekPerfStat, GlobRepository repository) throws Exception {
    int cohortSize = weekPerfStat.get(WeekStats.NEW_USERS);

    double activationRatio = getRatio(weekPerfStat, cohortSize, WeekStats.ACTIVATION_COUNT);
    double retentionRatio = getRatio(weekPerfStat, cohortSize, WeekStats.RETENTION_COUNT);
    double revenueRatio = getRatio(weekPerfStat, cohortSize, WeekStats.REVENUE_COUNT);

    repository.update(weekPerfStat,
                      value(WeekStats.ACTIVATION_RATIO, activationRatio),
                      value(WeekStats.RETENTION_RATIO, retentionRatio),
                      value(WeekStats.REVENUE_RATIO, revenueRatio));
  }

  private double getRatio(Glob weekPerfStat, int cohortSize, IntegerField field) {
    return AnalyticsUtils.ratio(weekPerfStat.get(field), cohortSize);
  }
}
