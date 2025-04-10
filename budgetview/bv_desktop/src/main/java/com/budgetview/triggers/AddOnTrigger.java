package com.budgetview.triggers;

import com.budgetview.model.AddOns;
import com.budgetview.model.User;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;

public class AddOnTrigger extends TypeChangeSetListener {
  public AddOnTrigger() {
    super(User.TYPE);
  }

  public void update(GlobRepository repository) {
    alignWithUser(repository);
  }

  public static void alignWithUser(GlobRepository repository) {
    Glob user = repository.find(User.KEY);
    if (user != null && repository.contains(AddOns.KEY)) {
      AddOns.setAllEnabled(repository, true);
    }
  }
}
