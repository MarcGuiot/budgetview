package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.WeekStat;
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
    if (weekStat.get(WeekStat.NEW_USERS) > 0) {
      retention = (double)weekStat.get(WeekStat.RETAINED_USERS) / (double)weekStat.get(WeekStat.NEW_USERS);
    }
    repository.update(weekStat.getKey(), WeekStat.RETENTION_RATIO, round2(retention));
  }

  private void updateRevenue(Glob weekStat, GlobRepository repository) {
    double revenue = 0;
    if (weekStat.get(WeekStat.POTENTIAL_BUYERS) > 0) {
      revenue = (double)weekStat.get(WeekStat.PURCHASES) / (double)weekStat.get(WeekStat.POTENTIAL_BUYERS);
    }
    repository.update(weekStat.getKey(), WeekStat.REVENUE_RATIO, round2(revenue));
  }

  public static double round2(double value) {
    double result = value * 1000;
    result = Math.round(result);
    return result / 10;
  }

}
