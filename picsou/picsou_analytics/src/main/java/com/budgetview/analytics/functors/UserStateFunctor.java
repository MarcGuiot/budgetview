package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.LogPeriod;
import com.budgetview.analytics.model.User;
import com.budgetview.analytics.utils.Days;
import com.budgetview.analytics.utils.Weeks;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.utils.Strings;
import org.joda.time.DateTime;

import static org.globsframework.model.FieldValue.value;

public class UserStateFunctor implements GlobFunctor {

  private int arbitraryStartWeek;

  public UserStateFunctor(GlobRepository repository) {
    arbitraryStartWeek = Weeks.getWeekId(new DateTime(LogPeriod.getFirstDate(repository)).minusDays(60));
  }

  public void run(Glob user, GlobRepository repository) throws Exception {

    boolean isPreviousUser = Strings.isNotEmpty(user.get(User.EMAIL)) && (user.get(User.PURCHASE_DATE)) == null;

    int cohortWeek;
    if (isPreviousUser) {
      cohortWeek = arbitraryStartWeek;
    }
    else {
      cohortWeek = Weeks.getWeekId(user.get(User.FIRST_DATE));
    }

    int usagePeriodLength = Days.daysBetween(user.get(User.FIRST_DATE), user.get(User.LAST_DATE));
    int pings = user.get(User.PING_COUNT, 0);
    double weeklyUsage = usagePeriodLength == 0 ? 0 : (double)pings / ((double)usagePeriodLength / 7);
    boolean activated = pings > 2 && usagePeriodLength > 2;
    boolean retained = pings > 5 && usagePeriodLength > 60;
    boolean purchased = isPreviousUser || user.get(User.PURCHASE_DATE) != null;
    boolean lost = Days.daysBetween(user.get(User.LAST_DATE), LogPeriod.getLastDate(repository)) > 60;

    repository.update(user.getKey(),
                      value(User.COHORT_WEEK, cohortWeek),
                      value(User.PREVIOUS_USER, isPreviousUser),
                      value(User.ACTIVATED, activated),
                      value(User.RETAINED, retained),
                      value(User.PURCHASED, purchased),
                      value(User.LOST, lost),
                      value(User.WEEKLY_USAGE, weeklyUsage)
                      );


  }
}
