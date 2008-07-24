package org.designup.picsou.server.persistence.prevayler;

import org.designup.picsou.server.session.Persistence;
import org.globsframework.model.Glob;
import org.globsframework.utils.T3uples;

public interface RootDataManager {

  Glob getHiddenUser(String linkInfo);

  Glob getUser(String name);

  void register(byte[] mail, byte[] signature);

  Persistence.UserInfo createUserAndHiddenUser(String name, boolean isRegisteredUser, byte[] cryptedPassword,
                                               byte[] linkInfo, byte[] cryptedLinkInfo);

  void deleteUser(String name, byte[] cryptedLinkInfo);

  void close();

  T3uples<byte[], byte[], Long> getAccountInfo();
}
