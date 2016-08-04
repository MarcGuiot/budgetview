package com.budgetview.desktop.userconfig.states;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public interface UserState {

  Boolean isVerifiedServerValidity();

  UserState fireKillUser(boolean mailSent);

  UserState fireValidUser();

  UserState updateUserValidity(GlobRepository repository, Directory directory);
}
