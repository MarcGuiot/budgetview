package com.budgetview.desktop.config.states;

import com.budgetview.desktop.config.UserState;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class CompletedUserState implements UserState {
  private String mail;

  public CompletedUserState(String mail) {
    this.mail = mail;
  }

  public Boolean isVerifiedServerValidity() {
    return true;
  }

  public UserState fireKillUser(boolean mailSent) {
    return this;
  }

  public UserState fireValidUser() {
    return this;
  }

  public UserState updateUserValidity(Directory directory, GlobRepository repository) {
    return this;
  }

}
