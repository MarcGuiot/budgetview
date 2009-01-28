package org.designup.picsou.gui.config;

import org.designup.picsou.model.User;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

class KilledUser implements UserState {
  private boolean mailSent;

  public KilledUser(boolean mailSent) {
    this.mailSent = mailSent;
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
    repository.startChangeSet();
    try {
      repository.update(User.KEY, User.IS_REGISTERED_USER, false);
      if (mailSent) {
        repository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_FAIL_MAIL_SEND);
      }
      else {
        repository.update(User.KEY, User.ACTIVATION_STATE, User.ACTIVATION_FAIL);
      }
    }
    finally {
      repository.completeChangeSet();
    }
    return new CompletedUserState();
  }

}
