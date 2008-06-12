package org.designup.picsou.server.persistence.prevayler;

import org.crossbowlabs.globs.model.Glob;
import org.designup.picsou.server.session.Persistence;

public interface RootDataManager {

  Glob getHiddenUser(String linkInfo);

  Glob getUser(String name);

  Persistence.UserInfo createUserAndHiddenUser(String name, boolean isRegisteredUser, byte[] cryptedPassword,
                                               byte[] linkInfo, byte[] cryptedLinkInfo);

  void deleteUser(String name, byte[] cryptedLinkInfo);

  void close();

}
