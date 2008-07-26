package org.designup.picsou.server.persistence.prevayler;

import org.designup.picsou.server.session.Persistence;
import org.globsframework.model.Glob;

public interface RootDataManager {

  Glob getHiddenUser(String linkInfo);

  Glob getUser(String name);

  void register(byte[] mail, byte[] signature);

  Persistence.UserInfo createUserAndHiddenUser(String name, boolean isRegisteredUser, byte[] cryptedPassword,
                                               byte[] linkInfo, byte[] cryptedLinkInfo);

  void deleteUser(String name, byte[] cryptedLinkInfo);

  void close();

  static class RepoInfo {
    private byte[] id;
    private byte[] mail;
    private byte[] signature;
    private String activationCode;
    private long count;

    public RepoInfo(byte[] id, byte[] mail, byte[] signature, String activationCode, long count) {
      this.id = id;
      this.mail = mail;
      this.signature = signature;
      this.activationCode = activationCode;
      this.count = count;
    }

    public byte[] getId() {
      return id;
    }

    public byte[] getMail() {
      return mail;
    }

    public byte[] getSignature() {
      return signature;
    }

    public long getCount() {
      return count;
    }

    public String getActivationCode() {
      return activationCode;
    }
  }

  RepoInfo getAndUpdateAccountInfo();
}
