package org.designup.picsou.server.persistence.prevayler;

import org.designup.picsou.server.persistence.prevayler.users.*;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;

import java.util.Date;

public class InMemoryRootDataManager implements RootDataManager {
  PRootData data = new PRootData();

  public Glob getHiddenUser(String linkInfo) {
    return data.getHiddenUser(linkInfo);
  }

  public Glob getUser(String name) {
    return data.getUser(name);
  }

  public void register(byte[] mail, byte[] signature, String activationCode) {
    Register register = new Register(mail, signature, activationCode);
    register.executeOn(data, new Date());
  }

  public Persistence.UserInfo createUserAndHiddenUser(String name, boolean autoLog, boolean isRegisteredUser, byte[] cryptedPassword,
                                          byte[] linkInfo, byte[] cryptedLinkInfo, Integer userId) {
    CreateUserAndHiddenUser user = new CreateUserAndHiddenUser(name, autoLog, isRegisteredUser, cryptedPassword,
                                                               linkInfo, cryptedLinkInfo, userId);
    return (Persistence.UserInfo)user.executeAndQuery(data, new Date());
  }

  public void deleteUser(String name, byte[] cryptedLinkInfo) {
    DeleteUserAndHiddenUser user = new DeleteUserAndHiddenUser(name, cryptedLinkInfo);
    user.executeOn(data, new Date());
  }

  public void close() {
  }

  public GlobList getLocalUsers() {
    return data.getLocalUsers();
  }

  public Integer allocateNewUserId(String name) {
    AllocateNewUserId id = new AllocateNewUserId(name);
    return (Integer)id.executeAndQuery(data, new Date());
  }

  public void replaceUserAndHiddenUser(boolean autoLog, boolean isRegisteredUser, String newName, byte[] newCryptedPassword, byte[] newLinkInfo, byte[] newCryptedLinkInfo, String name, byte[] linkInfo, byte[] cryptedLinkInfo, Integer userId) {
    RenameUserAndHiddenUser user = new RenameUserAndHiddenUser(autoLog, isRegisteredUser, newName, newCryptedPassword, newLinkInfo, newCryptedLinkInfo,
                                                               name, linkInfo, cryptedLinkInfo, userId);
    user.executeAndQuery(data, new Date());
  }

  public void setDownloadedVersion(long version) {
  }

  public void setLang(String lang) {
  }

  public RepoInfo getAndUpdateAccountInfo(long version) {
    return data.getRepoInfo();
  }
}
