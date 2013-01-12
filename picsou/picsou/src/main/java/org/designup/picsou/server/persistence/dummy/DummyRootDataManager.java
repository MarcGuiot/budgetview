package org.designup.picsou.server.persistence.dummy;

import org.designup.picsou.server.model.HiddenUser;
import org.designup.picsou.server.model.User;
import org.designup.picsou.server.persistence.prevayler.RootDataManager;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.serialization.Encoder;

public class DummyRootDataManager implements RootDataManager {
  private byte[] cryptedPassword;
  private byte[] linkInfo;
  private byte[] cryptedLinkInfo;
  private boolean autoLog;
  private static final int USER_ID = 1;

  public Glob getHiddenUser(String linkInfo) {
    return GlobBuilder.create(HiddenUser.TYPE,
                              FieldValue.value(HiddenUser.ENCRYPTED_LINK_INFO, Encoder.byteToString(cryptedLinkInfo)),
                              FieldValue.value(HiddenUser.USER_ID, USER_ID));
  }

  public Glob getUser(String name) {
    return GlobBuilder.create(User.TYPE, FieldValue.value(User.NAME, "demo"),
                              FieldValue.value(User.ENCRYPTED_PASSWORD, cryptedPassword),
                              FieldValue.value(User.LINK_INFO, linkInfo),
                              FieldValue.value(User.IS_REGISTERED_USER, true));
  }

  public void register(byte[] mail, byte[] signature, String activationCode) {
  }

  public Persistence.UserInfo createUserAndHiddenUser(String name, boolean autoLog, boolean isRegisteredUser,
                                                      byte[] cryptedPassword, byte[] linkInfo, byte[] cryptedLinkInfo,
                                                      Integer id) {
    this.autoLog = autoLog;
    this.cryptedPassword = cryptedPassword;
    this.linkInfo = linkInfo;
    this.cryptedLinkInfo = cryptedLinkInfo;
    return new Persistence.UserInfo(id, true);
  }

  public void deleteUser(String name, byte[] cryptedLinkInfo) {
  }

  public void close() {
  }

  public GlobList getLocalUsers() {
    return GlobList.EMPTY;
  }

  public Integer allocateNewUserId(String name) {
    return USER_ID;
  }

  public void replaceUserAndHiddenUser(boolean autoLog, boolean isRegisteredUser, String newName, byte[] newCryptedPassword, byte[] newLinkInfo, byte[] newCryptedLinkInfo, String name, byte[] linkInfo, byte[] cryptedLinkInfo, Integer userId) {
  }

  public void setDownloadedVersion(long version) {
  }

  public void setLang(String lang) {
  }

  public RepoInfo getAndUpdateAccountInfo() {
    return new RepoInfo(new byte[0], new byte[0], new byte[0], "", 0, 0, "en");
  }
}
