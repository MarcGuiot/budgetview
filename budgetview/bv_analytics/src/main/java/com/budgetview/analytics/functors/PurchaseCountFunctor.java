package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.User;
import com.budgetview.analytics.model.WeekStats;
import com.budgetview.analytics.utils.Weeks;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;

import java.util.Date;

public class PurchaseCountFunctor implements GlobFunctor {

  public void run(Glob user, GlobRepository repository) throws Exception {

    Date purchaseDate = user.get(User.PURCHASE_DATE);
    if (purchaseDate == null) {
      return;
    }

    int purchaseWeek = Weeks.getWeekId(purchaseDate);

    Glob weekStat =
      repository.findOrCreate(org.globsframework.model.Key.create(WeekStats.TYPE, purchaseWeek));
    repository.update(weekStat, WeekStats.NEW_PURCHASES, weekStat.get(WeekStats.NEW_PURCHASES) + 1);
  }
}
