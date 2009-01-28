package org.designup.picsou.gui.config;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

class LocallyValidUser implements UserState {

  LocallyValidUser() {
  }

  synchronized public Boolean isVerifiedServerValidity() {
    return false;
  }

  public UserState fireKillUser(boolean mailSent) {
    return new KilledUser(mailSent);
  }

  public UserState fireValidUser() {
    return new ValidUser();
  }

  public UserState updateUserValidity(Directory directory, GlobRepository repository) {
    throw new InvalidState(getClass().toString());
  }

}
