package org.designup.picsou.server.persistence.prevayler;

import org.designup.picsou.client.exceptions.BadPassword;
import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.designup.picsou.client.exceptions.UserNotRegistered;
import org.designup.picsou.server.model.HiddenUser;
import org.designup.picsou.server.model.User;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.utils.Log;
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

  public UserInfo createUser(String name, boolean isRegisteredUser, byte[] cryptedPassword, byte[] linkInfo, byte[] cryptedLinkInfo) {
    return rootDataManager.createUserAndHiddenUser(name, isRegisteredUser, cryptedPassword, linkInfo, cryptedLinkInfo);
  }

  public void delete(String name, byte[] cryptedPassword, byte[] linkInfo, byte[] cryptedLinkInfo, Integer userId) {
    accountDataManager.delete(userId);
  }

  public Glob getUser(String name) {
    return rootDataManager.getUser(name);
  }

  public Glob getHiddenUser(byte[] cryptedLinkInfo) {
    return rootDataManager.getHiddenUser(Encoder.b64Decode(cryptedLinkInfo));
  }

  public GlobList getHiddenGlob(GlobType type, Integer userId) {
    return getFiltered(userId, type);
  }

  public void close() {
    rootDataManager.close();
    accountDataManager.close();
  }

  public void close(Integer userId) {
    accountDataManager.close(userId);
  }

  public void takeSnapshot(Integer userId) {
    accountDataManager.takeSnapshot(userId);
  }

  private GlobList getFiltered(int userId, GlobType globType) {
    GlobList list = accountDataManager.getUserData(userId);
    for (java.util.Iterator it = list.iterator(); it.hasNext();) {
      Glob glob = (Glob)it.next();
      if (glob.getType() != globType) {
        it.remove();
      }
    }
    return list;
  }

  public void getData(SerializedOutput output, Integer userId) {
    accountDataManager.getUserData(output, userId);
  }

  public void updateData(SerializedInput input, SerializedOutput output, Integer userId) {
    accountDataManager.updateUserData(input, userId);
  }

  public Integer getNextId(String globTypeName, Integer count, Integer userId) {
    return accountDataManager.getNextId(globTypeName, userId, count);
  }

}
