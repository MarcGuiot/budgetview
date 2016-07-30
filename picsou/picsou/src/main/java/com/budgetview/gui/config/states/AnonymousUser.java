package com.budgetview.gui.config.states;

import com.budgetview.gui.config.UserState;
import com.budgetview.model.User;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class AnonymousUser implements UserState {
  boolean verifiedServerValidity = false;
  private String mail;

  public AnonymousUser(String mail) {
    this.mail = mail;
  }

  public AnonymousUser(boolean verifiedServerValidity) {
    this.verifiedServerValidity = verifiedServerValidity;
  }

  synchronized public Boolean isVerifiedServerValidity() {
    return verifiedServerValidity;
  }

  synchronized public UserState fireKillUser(boolean mailSent) {
    verifiedServerValidity = true;
    return this;
  }

  synchronized public UserState fireValidUser() {
    verifiedServerValidity = true;
    return this;
  }

  public UserState updateUserValidity(Directory directory, GlobRepository repository) {
    repository.startChangeSet();
    try {
      repository.update(User.KEY, value(User.IS_REGISTERED_USER, false));
    }
    finally {
      repository.completeChangeSet();
    }
    return this;
  }

}
