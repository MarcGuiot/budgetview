package org.designup.picsou.server.persistence.prevayler;

import org.designup.picsou.client.exceptions.BadPassword;
import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.designup.picsou.client.exceptions.UserNotRegistered;
import org.designup.picsou.client.exceptions.RemoteException;
import org.designup.picsou.server.model.HiddenUser;
import org.designup.picsou.server.model.User;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.serialization.Encoder;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.Arrays;

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
      Log.write("For " + name + " bad password");
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
//    if (rootDataManager == null){
//      Utils.dumpStack();
//    }
    rootDataManager.register(mail, signature, activationCode);
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

  public void getData(SerializedOutput output, Integer userId) {
    accountDataManager.getUserData(output, userId);
  }

  public void updateData(SerializedInput input, SerializedOutput output, Integer userId) {
    accountDataManager.updateUserData(input, userId);
  }

  public void connect(SerializedOutput output) {
    RootDataManager.RepoInfo accountInfo = rootDataManager.getAndUpdateAccountInfo();
    output.write(true);
    output.writeBytes(accountInfo.getId());
    output.writeBytes(accountInfo.getMail());
    output.writeBytes(accountInfo.getSignature());
    output.writeJavaString(accountInfo.getActivationCode());
    output.write(accountInfo.getCount());
  }

}
