package com.budgetview.desktop.userconfig.states;

import com.budgetview.model.LicenseActivationState;
import com.budgetview.model.User;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

public class KilledUser implements UserState {
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

  public UserState updateUserValidity(GlobRepository repository, Directory directory) {
    repository.startChangeSet();
    try {
      repository.update(User.KEY, User.IS_REGISTERED_USER, false);
      repository.update(User.KEY, User.EMAIL, mail);
      if (mailSent) {
        repository.update(User.KEY, User.LICENSE_ACTIVATION_STATE, LicenseActivationState.STARTUP_CHECK_MAIL_SENT.getId());
      }
      else {
        repository.update(User.KEY, User.LICENSE_ACTIVATION_STATE, LicenseActivationState.STARTUP_CHECK_KILL_USER.getId());
      }
    }
    finally {
      repository.completeChangeSet();
    }
    return new CompletedUserState(mail);
  }

}
