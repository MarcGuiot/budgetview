package com.budgetview.desktop.userconfig.states;

import com.budgetview.model.User;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import static org.globsframework.model.FieldValue.value;

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

  public UserState updateUserValidity(GlobRepository repository, Directory directory) {
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
