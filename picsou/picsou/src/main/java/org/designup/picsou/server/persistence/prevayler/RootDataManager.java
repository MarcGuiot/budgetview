package org.designup.picsou.server.persistence.prevayler;

import org.crossbowlabs.globs.model.Glob;

public interface RootDataManager {

  class UserInfo {
    final public Integer userId;
    final public boolean isRegistered;

    public UserInfo(Integer userId, boolean registered) {
      this.userId = userId;
      isRegistered = registered;
    }
  }

  Glob getHiddenUser(String linkInfo);

  Glob getUser(String name);

  UserInfo createUserAndHiddenUser(String name, boolean isRegisteredUser, byte[] cryptedPassword,
                                   byte[] linkInfo, byte[] cryptedLinkInfo);

  void deleteUser(String name, byte[] cryptedLinkInfo);

  void close();

}
