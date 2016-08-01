package com.budgetview.server.session;

import com.budgetview.client.exceptions.IdentificationFailed;
import com.budgetview.server.persistence.prevayler.AccountDataManager;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.List;

public interface Persistence {
  UserInfo createUser(String name, boolean autoLog, boolean isRegisteredUser,
                      byte[] encryptedPassword, byte[] linkInfo, byte[] encryptedLinkInfo);

  void getData(SerializedOutput output, Integer userId);

  void updateData(SerializedInput input, SerializedOutput output, Integer userId);

  void connect(SerializedOutput output, long version);

  Glob identify(String name, byte[] encryptedPassword);

  Integer confirmUser(String b64LinkInfo) throws IdentificationFailed;

  void register(byte[] mail, byte[] signature, String activationCode);

  void delete(String name, byte[] encryptedLinkInfo, Integer userId);

  Glob getUser(String name);

  Glob getHiddenUser(byte[] encryptedLinkInfo);

  void close();

  void close(Integer userId);

  void takeSnapshot(Integer userId);

  boolean restore(SerializedInput input, Integer userId);

  GlobList getLocalUsers();

  Integer renameUser(String newName, String name, boolean autoLog, byte[] password,
                     byte[] previousLinkInfo, byte[] previousEncryptedLinkInfo,
                     byte[] linkInfo, byte[] encryptedLinkInfo,
                     Integer previousUserId, SerializedInput input);

  void setDownloadedVersion(long version);

  void setLang(String lang);

  List<AccountDataManager.SnapshotInfo> getSnapshotInfos(Integer userId);

  void getSnapshotData(String fileName, SerializedOutput output, Integer userId);

  boolean hasChanged(Integer userId);

  class UserInfo {
    final public Integer userId;
    final public boolean isRegistered;

    public UserInfo(Integer userId, boolean registered) {
      this.userId = userId;
      isRegistered = registered;
    }
  }
}
