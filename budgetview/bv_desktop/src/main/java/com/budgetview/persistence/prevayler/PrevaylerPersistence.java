package com.budgetview.persistence.prevayler;

import com.budgetview.client.exceptions.BadPassword;
import com.budgetview.client.exceptions.IdentificationFailed;
import com.budgetview.client.exceptions.RemoteException;
import com.budgetview.client.exceptions.UserNotRegistered;
import com.budgetview.session.model.HiddenUser;
import com.budgetview.session.model.User;
import com.budgetview.session.states.Persistence;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.serialization.Encoder;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.Arrays;
import java.util.List;

public class PrevaylerPersistence implements Persistence {
  private RootDataManager rootDataManager;
  private AccountDataManager accountDataManager;
  private Directory directory;

  public PrevaylerPersistence(AccountDataManager accountDataManager, RootDataManager rootDataManager,
                              Directory directory) {
    this.accountDataManager = accountDataManager;
    this.rootDataManager = rootDataManager;
    this.directory = directory;
  }

  public Glob identify(String name, byte[] cryptedPassword) {
    Glob user = rootDataManager.getUser(name);
    if (user == null) {
      throw new UserNotRegistered(name + " not registered");
    }
    if (!Arrays.equals(user.get(User.ENCRYPTED_PASSWORD), cryptedPassword)) {
      Log.write("[Persistence] For " + name + " bad password");
      throw new BadPassword(name + " not identified correctly");
    }
    return user;
  }

  public Integer confirmUser(String b64LinkInfo) throws IdentificationFailed {
    Glob hiddenUser = rootDataManager.getHiddenUser(b64LinkInfo);
    if (hiddenUser == null) {
      throw new InvalidData("User recognized but no hiddenUser associated");
    }
    return hiddenUser.get(HiddenUser.USER_ID);
  }

  public void register(byte[] mail, byte[] signature, String activationCode) {
    if (rootDataManager != null){
      // le thread LicenseCheckerThread fini par passé mais dans le test d'apres alors que
      // le closed a été appelé.
      rootDataManager.register(mail, signature, activationCode);
    }
  }

  public UserInfo createUser(String name, boolean autoLog, boolean isRegisteredUser, byte[] cryptedPassword,
                             byte[] linkInfo, byte[] cryptedLinkInfo) {
    Integer newUserId = rootDataManager.allocateNewUserId(name);
    return rootDataManager.createUserAndHiddenUser(name, autoLog, isRegisteredUser, cryptedPassword,
                                                   linkInfo, cryptedLinkInfo, newUserId);
  }

  public void delete(String name, byte[] cryptedLinkInfo, Integer userId) {
    rootDataManager.deleteUser(name, cryptedLinkInfo);
    accountDataManager.delete(userId);
  }

  public Glob getUser(String name) {
    return rootDataManager.getUser(name);
  }

  public Glob getHiddenUser(byte[] cryptedLinkInfo) {
    return rootDataManager.getHiddenUser(Encoder.byteToString(cryptedLinkInfo));
  }

  public void close() {
    rootDataManager.close();
    accountDataManager.close();
    accountDataManager = null;
    rootDataManager = null;
  }

  public void close(Integer userId) {
    accountDataManager.close(userId);
  }

  public void takeSnapshot(Integer userId) {
    accountDataManager.takeSnapshot(userId);
  }

  public List<AccountDataManager.SnapshotInfo> getSnapshotInfos(Integer userId){
    return accountDataManager.getSnapshotInfos(userId);
  }

  public void getSnapshotData(String fileName, SerializedOutput output, Integer userId){
    accountDataManager.getSnapshotData(userId, fileName, output);
  }

  public boolean hasChanged(Integer userId) {
    return accountDataManager.hasChanged(userId);
  }

  public boolean restore(SerializedInput input, Integer userId) {
    return accountDataManager.restore(input, userId);
  }

  public GlobList getLocalUsers() {
    return rootDataManager.getLocalUsers();
  }

  public Integer renameUser(String newName, String name, boolean autoLog, byte[] cryptedPassword,
                            byte[] previousLinkInfo, byte[] previousEncryptedLinkInfo,
                            byte[] linkInfo, byte[] encryptedLinkInfo,
                            Integer previousUserId, SerializedInput input) throws RemoteException {
    Integer newUserId = rootDataManager.allocateNewUserId(name);
    if (!accountDataManager.newData(newUserId, input)){
      return null;
    }
    rootDataManager.replaceUserAndHiddenUser(autoLog, false,  newName, cryptedPassword, linkInfo, encryptedLinkInfo, 
                                             name, previousLinkInfo, previousEncryptedLinkInfo, newUserId);
//    accountDataManager.deleteOldData(previousUserId);
//    rootDataManager.deleteOldUserId(previousEncryptedLinkInfo, previousUserId);
    return newUserId;
  }

  public void setDownloadedVersion(long version) {
    rootDataManager.setDownloadedVersion(version);
  }

  public void setLang(String lang) {
    rootDataManager.setLang(lang);
  }

  public void getData(SerializedOutput output, Integer userId) {
    accountDataManager.getUserData(output, userId);
  }

  public void updateData(SerializedInput input, SerializedOutput output, Integer userId) {
    accountDataManager.updateUserData(input, userId);
  }

  public void connect(SerializedOutput output, long version) {
    RootDataManager.RepoInfo accountInfo = rootDataManager.getAndUpdateAccountInfo(version);
    output.write(true);
    output.writeBytes(accountInfo.getId());
    output.writeBytes(accountInfo.getMail());
    output.writeBytes(accountInfo.getSignature());
    output.writeJavaString(accountInfo.getActivationCode());
    output.write(accountInfo.getCount());
    output.write(accountInfo.getDownloadedVersion());
    output.writeUtf8String(accountInfo.getLang());
    output.write(accountInfo.getVersion());
  }

}
