package org.designup.picsou.triggers;

import org.designup.picsou.model.AddOns;
import org.designup.picsou.model.User;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;

public class AddOnTrigger extends TypeChangeSetListener {
  public AddOnTrigger() {
    super(User.TYPE);
  }

  public void update(GlobRepository repository) {
    Glob user = repository.find(User.KEY);
    if (user != null && repository.contains(AddOns.KEY)) {
      AddOns.setAllEnabled(repository, user.isTrue(User.IS_REGISTERED_USER));
    }
  }
}
