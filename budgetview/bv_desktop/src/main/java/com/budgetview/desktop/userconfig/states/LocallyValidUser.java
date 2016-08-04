package com.budgetview.desktop.userconfig.states;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

public class LocallyValidUser implements UserState {
  private String mail;

  public LocallyValidUser(String mail) {
    this.mail = mail;
  }

  synchronized public Boolean isVerifiedServerValidity() {
    return false;
  }

  public UserState fireKillUser(boolean mailSent) {
    return new KilledUser(mail, mailSent);
  }

  public UserState fireValidUser() {
    return new ValidUser(mail);
  }

  public UserState updateUserValidity(GlobRepository repository, Directory directory) {
    throw new InvalidState(getClass().toString());
  }

}
