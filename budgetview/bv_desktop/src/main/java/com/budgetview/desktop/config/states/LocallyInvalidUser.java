package com.budgetview.desktop.config.states;

import com.budgetview.desktop.config.UserState;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

public class LocallyInvalidUser implements UserState {
  private String mail;

  public LocallyInvalidUser(String mail) {
    this.mail = mail;
  }

  synchronized public Boolean isVerifiedServerValidity() {
    return false;
  }

  public UserState fireKillUser(boolean mailSent) {
    return new AnonymousUser(true);
  }

  public UserState fireValidUser() {
    return new AnonymousUser(true);
  }

  public UserState updateUserValidity(Directory directory, GlobRepository repository) {
    throw new InvalidState(getClass().toString());
  }

}
