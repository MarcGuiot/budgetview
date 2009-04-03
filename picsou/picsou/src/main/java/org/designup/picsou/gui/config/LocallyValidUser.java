package org.designup.picsou.gui.config;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

class LocallyValidUser implements UserState {
  private String mail;

  LocallyValidUser(String mail) {
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

  public UserState updateUserValidity(Directory directory, GlobRepository repository) {
    throw new InvalidState(getClass().toString());
  }

}
