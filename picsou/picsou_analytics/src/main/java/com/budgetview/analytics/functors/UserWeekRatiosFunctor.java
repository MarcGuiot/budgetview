package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.WeekPerfStat;
import com.budgetview.analytics.utils.AnalyticsUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;

public class UserWeekRatiosFunctor implements GlobFunctor {
  public void run(Glob weekStat, GlobRepository repository) throws Exception {
    updateRetention(weekStat, repository);
    updateRevenue(weekStat, repository);
  }

  private void updateRetention(Glob weekStat, GlobRepository repository) {
    double retention = 0;
    if (weekStat.get(WeekPerfStat.NEW_USERS) > 0) {
      retention = (double)weekStat.get(WeekPerfStat.RETAINED_USERS) / (double)weekStat.get(WeekPerfStat.NEW_USERS);
    }
    repository.update(weekStat.getKey(), WeekPerfStat.RETENTION_RATIO, AnalyticsUtils.round2(retention));
  }

  private void updateRevenue(Glob weekStat, GlobRepository repository) {
    double revenue = 0;
    if (weekStat.get(WeekPerfStat.POTENTIAL_BUYERS) > 0) {
      revenue = (double)weekStat.get(WeekPerfStat.PURCHASES) / (double)weekStat.get(WeekPerfStat.POTENTIAL_BUYERS);
    }
    repository.update(weekStat.getKey(), WeekPerfStat.REVENUE_RATIO, AnalyticsUtils.round2(revenue));
  }
}
