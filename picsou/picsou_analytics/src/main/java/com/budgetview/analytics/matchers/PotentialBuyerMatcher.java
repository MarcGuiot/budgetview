package com.budgetview.analytics.matchers;

import com.budgetview.analytics.model.User;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Date;

public class PotentialBuyerMatcher implements GlobMatcher {

  private DateTime endDate;

  public PotentialBuyerMatcher(Date endDate) {
    this.endDate = new DateTime(endDate);
  }

  public boolean matches(Glob user, GlobRepository repository) {
    if (user.isTrue(User.PREVIOUS_USER) || user.get(User.PURCHASE_DATE) != null) {
      return false;
    }
    
    DateTime firstTry = new DateTime(user.get(User.FIRST_DATE));
    if (firstTry.isAfter(endDate)) {
      return false;
    }

    DateTime lastTry = new DateTime(user.get(User.LAST_DATE));
    return Days.daysBetween(firstTry, lastTry).getDays() > 0 &&
           Days.daysBetween(firstTry, endDate).getDays() < 45 &&
           Days.daysBetween(lastTry, endDate).getDays() < 20;
  }
}
