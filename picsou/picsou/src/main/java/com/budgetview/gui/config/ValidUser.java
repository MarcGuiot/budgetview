package com.budgetview.gui.config;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

class ValidUser implements UserState {
  private String mail;

  public ValidUser(String mail) {
    this.mail = mail;
  }

  public Boolean isVerifiedServerValidity() {
    return true;
  }

  public UserState fireKillUser(boolean mailSent) {
    throw new InvalidState(getClass().toString());
  }

  public UserState fireValidUser() {
    throw new InvalidState(getClass().toString());
  }

  public UserState updateUserValidity(Directory directory, GlobRepository repository) {
    return new CompletedUserState(mail);
  }

}
