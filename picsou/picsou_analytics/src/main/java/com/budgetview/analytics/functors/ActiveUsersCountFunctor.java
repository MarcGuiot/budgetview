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

public class ActiveUsersCountFunctor implements GlobFunctor {

  private int firstLogWeek;

  public ActiveUsersCountFunctor(GlobRepository repository) {
    firstLogWeek = Weeks.getWeekId(LogPeriod.getFirstDate(repository));
  }

  public void run(Glob user, GlobRepository repository) throws Exception {

    int firstWeek = Math.max(firstLogWeek, user.get(User.COHORT_WEEK));
    int lastWeek = Weeks.getWeekId(user.get(User.LAST_DATE));
    int purchaseWeek = getPurchaseWeek(user);
    for (int week = firstWeek; week <= lastWeek; week = Weeks.next(week)) {
      Glob weekStat =
        repository.findOrCreate(org.globsframework.model.Key.create(WeekStats.TYPE, week));

      int total = increment(weekStat, WeekStats.TOTAL_ACTIVE_USERS, user.isTrue(User.ACTIVATED));
      int paid = increment(weekStat, WeekStats.TOTAL_PAID_ACTIVE_USERS, week > purchaseWeek);

      repository.update(weekStat,
                        value(WeekStats.TOTAL_ACTIVE_USERS, total),
                        value(WeekStats.TOTAL_PAID_ACTIVE_USERS, paid));
    }
  }

  public int getPurchaseWeek(Glob user) {
    if (!user.isTrue(User.PURCHASED)) {
      return Integer.MAX_VALUE;
    }
    if (user.isTrue(User.PREVIOUS_USER)) {
      return Integer.MIN_VALUE;
    }
    return Weeks.getWeekId(user.get(User.PURCHASE_DATE));
  }

  public int increment(Glob weekStat, IntegerField field, boolean doIncrement) {
    return weekStat.get(field, 0) + (doIncrement ? 1 : 0);
  }
}
