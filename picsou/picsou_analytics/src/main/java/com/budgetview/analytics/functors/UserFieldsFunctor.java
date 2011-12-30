package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.User;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.utils.Strings;
import org.joda.time.DateTime;
import org.joda.time.Days;

public class UserFieldsFunctor implements GlobFunctor {

  public void run(Glob user, GlobRepository repository) throws Exception {
    boolean isPreviousUser = Strings.isNotEmpty(user.get(User.EMAIL)) && (user.get(User.PURCHASE_DATE)) == null;
    repository.update(user.getKey(), User.PREVIOUS_USER, isPreviousUser);
  }
}
