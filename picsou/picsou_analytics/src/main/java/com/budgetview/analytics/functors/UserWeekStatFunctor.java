package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.User;
import com.budgetview.analytics.model.WeekStat;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
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
    Glob weekStat = getWeekStat(firstDate, repository);

    repository.update(weekStat.getKey(), WeekStat.NEW_USERS, weekStat.get(WeekStat.NEW_USERS) + 1);
    if (Days.daysBetween(firstDate, lastDate).getDays() > 0) {
      repository.update(weekStat.getKey(), WeekStat.RETAINED_USERS, weekStat.get(WeekStat.RETAINED_USERS) + 1);
    }
  }

  private void processPurchases(Glob user, GlobRepository repository) {
    Date date = user.get(User.PURCHASE_DATE);
    if (date == null) {
      return;
    }
    DateTime purchaseDate = new DateTime(date.getTime());
    Glob weekStat = getWeekStat(purchaseDate, repository);
    repository.update(weekStat.getKey(), WeekStat.PURCHASES, weekStat.get(WeekStat.PURCHASES) + 1);
  }

  private Glob getWeekStat(DateTime date, GlobRepository repository) {
    int weekId = date.getWeekyear() * 100 + date.getWeekOfWeekyear();
    Glob week = repository.find(Key.create(WeekStat.TYPE, weekId));
    if (week == null) {
      int dayOfWeek = date.getDayOfWeek();
      DateTime lastDayOfWeek = date.plusDays(7 - dayOfWeek);
      week = repository.create(WeekStat.TYPE,
                               value(WeekStat.ID, weekId),
                               value(WeekStat.LAST_DAY, lastDayOfWeek.toDate()));
    }
    return week;
  }
}
