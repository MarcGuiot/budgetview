package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.LogPeriod;
import com.budgetview.analytics.model.User;
import com.budgetview.analytics.model.WeekStats;
import com.budgetview.analytics.utils.Weeks;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;

import static org.globsframework.model.FieldValue.value;

public class CohortCountFunctor implements GlobFunctor {

  private int firstWeek;

  public CohortCountFunctor(GlobRepository repository) {
    firstWeek = Weeks.getWeekId(LogPeriod.getFirstDate(repository));
  }

  public void run(Glob user, GlobRepository repository) throws Exception {

    if (user.get(User.COHORT_WEEK) < firstWeek) {
      return;
    }

    Glob weekPerfStat =
      repository.findOrCreate(org.globsframework.model.Key.create(WeekStats.TYPE, user.get(User.COHORT_WEEK)));

    int count = increment(weekPerfStat, WeekStats.NEW_USERS, true);
    int activated = increment(weekPerfStat, WeekStats.ACTIVATION_COUNT, user.isTrue(User.ACTIVATED));
    int retained = increment(weekPerfStat, WeekStats.RETENTION_COUNT, user.isTrue(User.RETAINED));
    int purchased = increment(weekPerfStat, WeekStats.REVENUE_COUNT, user.get(User.PURCHASED));

    repository.update(weekPerfStat,
                      value(WeekStats.NEW_USERS, count),
                      value(WeekStats.ACTIVATION_COUNT, activated),
                      value(WeekStats.RETENTION_COUNT, retained),
                      value(WeekStats.REVENUE_COUNT, purchased));
  }

  public int increment(Glob weekPerfStat, IntegerField field, boolean doIncrement) {
    return weekPerfStat.get(field, 0) + (doIncrement ? 1 : 0);
  }
}
