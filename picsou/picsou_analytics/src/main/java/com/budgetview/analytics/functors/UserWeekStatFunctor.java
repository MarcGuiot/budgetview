package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.User;
import com.budgetview.analytics.model.WeekPerfStat;
import com.budgetview.analytics.utils.AnalyticsUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Date;

import static org.globsframework.model.FieldValue.value;

public class UserWeekStatFunctor implements GlobFunctor {
  public void run(Glob user, GlobRepository repository) throws Exception {
    if (user.isTrue(User.PREVIOUS_USER)) {
      return;
    }

    processRetention(user, repository);
    processPurchases(user, repository);
  }

  private void processRetention(Glob user, GlobRepository repository) {
    DateTime firstDate = new DateTime(user.get(User.FIRST_DATE).getTime());
    DateTime lastDate = new DateTime(user.get(User.LAST_DATE).getTime());
    Glob weekStat =
      AnalyticsUtils.getWeekStat(firstDate, WeekPerfStat.TYPE, WeekPerfStat.ID, WeekPerfStat.LAST_DAY, repository);

    repository.update(weekStat.getKey(), WeekPerfStat.NEW_USERS, weekStat.get(WeekPerfStat.NEW_USERS) + 1);
    if (Days.daysBetween(firstDate, lastDate).getDays() > 0) {
      repository.update(weekStat.getKey(), WeekPerfStat.RETAINED_USERS, weekStat.get(WeekPerfStat.RETAINED_USERS) + 1);
    }
  }

  private void processPurchases(Glob user, GlobRepository repository) {
    Date date = user.get(User.PURCHASE_DATE);
    if (date == null) {
      return;
    }

    Glob weekStat = AnalyticsUtils.getWeekStat(date, WeekPerfStat.TYPE, WeekPerfStat.ID, WeekPerfStat.LAST_DAY, repository);
    repository.update(weekStat.getKey(), WeekPerfStat.PURCHASES, weekStat.get(WeekPerfStat.PURCHASES) + 1);
  }
}
