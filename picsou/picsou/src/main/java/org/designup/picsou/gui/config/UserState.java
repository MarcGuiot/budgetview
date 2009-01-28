package org.designup.picsou.gui.config;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

interface UserState {

  Boolean isVerifiedServerValidity();

  UserState fireKillUser(boolean mailSent);

  UserState fireValidUser();

  UserState updateUserValidity(Directory directory, GlobRepository repository);
}
