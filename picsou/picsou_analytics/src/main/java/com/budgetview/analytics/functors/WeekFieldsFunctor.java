package com.budgetview.analytics.functors;

import com.budgetview.analytics.matchers.PotentialBuyerMatcher;
import com.budgetview.analytics.model.User;
import com.budgetview.analytics.model.WeekPerfStat;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;

public class WeekFieldsFunctor implements GlobFunctor {
  public void run(Glob weekStat, GlobRepository repository) throws Exception {
    GlobList potentialBuyers =
      repository.getAll(User.TYPE, new PotentialBuyerMatcher(weekStat.get(WeekPerfStat.LAST_DAY)));
    repository.update(weekStat.getKey(), WeekPerfStat.POTENTIAL_BUYERS, potentialBuyers.size());
  }
}
