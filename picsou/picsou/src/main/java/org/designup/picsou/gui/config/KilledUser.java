package org.designup.picsou.gui.config;

import org.designup.picsou.model.User;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

class KilledUser implements UserState {
  private String mail;
  private boolean mailSent;

  public KilledUser(String mail, boolean mailSent) {
    this.mail = mail;
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
      repository.update(User.KEY, User.EMAIL, mail);
      if (mailSent) {
        repository.update(User.KEY, User.ACTIVATION_STATE, User.STARTUP_CHECK_MAIL_SENT);
      }
      else {
        repository.update(User.KEY, User.ACTIVATION_STATE, User.STARTUP_CHECK_KILL_USER);
      }
    }
    finally {
      repository.completeChangeSet();
    }
    return new CompletedUserState(mail);
  }

}
