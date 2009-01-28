package org.designup.picsou.gui.config;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

class LocallyInvalidUser implements UserState {
  boolean verifiedServerValidity = false;

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
