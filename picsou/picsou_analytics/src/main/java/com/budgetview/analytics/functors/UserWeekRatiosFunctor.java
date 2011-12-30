package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.WeekStat;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;

public class UserWeekRatiosFunctor implements GlobFunctor {
  public void run(Glob weekStat, GlobRepository repository) throws Exception {
    if (weekStat.get(WeekStat.NEW_USERS) == 0) {
      return;
    }
    
    double retention = (double)weekStat.get(WeekStat.RETAINED_USERS) / (double)weekStat.get(WeekStat.NEW_USERS);
    repository.update(weekStat.getKey(), WeekStat.RETENTION_RATIO, retention);
  }
}
