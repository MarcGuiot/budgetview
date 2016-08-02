package com.budgetview.persistence.prevayler;

import com.budgetview.session.states.Persistence;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;

public interface RootDataManager {

  Glob getHiddenUser(String linkInfo);

  Glob getUser(String name);

  void register(byte[] mail, byte[] signature, String activationCode);

  Persistence.UserInfo createUserAndHiddenUser(String name, boolean autoLog, boolean isRegisteredUser, byte[] cryptedPassword,
                                               byte[] linkInfo, byte[] cryptedLinkInfo, Integer userId);

  void deleteUser(String name, byte[] cryptedLinkInfo);

  void close();

  GlobList getLocalUsers();

  Integer allocateNewUserId(String name);

  void replaceUserAndHiddenUser(boolean autoLog, boolean isRegisteredUser,
                                String newName, byte[] newCryptedPassword, byte[] newLinkInfo, byte[] newCryptedLinkInfo,
                                String name, byte[] linkInfo, byte[] cryptedLinkInfo,
                                Integer userId);

  void setDownloadedVersion(long version);

  void setLang(String lang);

  static class RepoInfo {
    private byte[] id;
    private byte[] mail;
    private byte[] signature;
    private String activationCode;
    private long count;
    private long downloadedVersion;
    private String lang;
    private long version;

    public RepoInfo(byte[] id, byte[] mail, byte[] signature, String activationCode, long count,
                    long downloadedVersion, String lang, long version) {
      this.id = id;
      this.mail = mail;
      this.signature = signature;
      this.activationCode = activationCode;
      this.count = count;
      this.downloadedVersion = downloadedVersion;
      this.lang = lang;
      this.version = version;
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

    public long getDownloadedVersion() {
      return downloadedVersion;
    }

    public String getLang() {
      return lang;
    }

    public long getVersion() {
      return version;
    }
  }

  RepoInfo getAndUpdateAccountInfo(long version);
}
