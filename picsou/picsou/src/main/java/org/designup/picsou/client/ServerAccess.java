package org.designup.picsou.client;

import org.designup.picsou.client.exceptions.BadPassword;
import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.client.exceptions.UserNotRegistered;
import org.designup.picsou.client.http.PasswordBasedEncryptor;
import org.designup.picsou.server.model.SerializableGlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.collections.MapOfMaps;

import java.util.List;

public interface ServerAccess {

  boolean createUser(String name, char[] password, boolean autoLog)
    throws UserAlreadyExists, IdentificationFailed, PasswordBasedEncryptor.EncryptFail;

  boolean initConnection(String name, char[] password, boolean privateComputer)
    throws BadPassword, UserNotRegistered;

  void localRegister(byte[] mail, byte[] signature, String activationCode, long jarVersion);

  void downloadedVersion(long version);

  void applyChanges(ChangeSet changeSet, GlobRepository globRepository);

  void takeSnapshot();

  LocalInfo connect();

  MapOfMaps<String, Integer, SerializableGlobType> getServerData();

  void replaceData(MapOfMaps<String, Integer, SerializableGlobType> data);

  void deleteUser(String name, char[] password);

  boolean rename(String name, char[] passwd, char[] previousPasswd) throws UserAlreadyExists;

  List<SnapshotInfo> getSnapshotInfos();

  MapOfMaps<String, Integer, SerializableGlobType> getSnapshotData(SnapshotInfo info, IdUpdater idUpdater);

  class UserInfo{
    public final String name;
    public final boolean autologin;

    public UserInfo(String name, Boolean autologin) {
      this.name = name;
      this.autologin = autologin;
    }
  }

  class SnapshotInfo implements Comparable<SnapshotInfo> {
    public final long timestamp;
    public final String file;
    public final String password;

    public SnapshotInfo(long timestamp, String file, String password) {
      this.timestamp = timestamp;
      this.file = file;
      this.password = password;
    }

    public int compareTo(SnapshotInfo other) {
      if (other.timestamp == timestamp) {
        return 0;
      }
      return other.timestamp < timestamp ? -1 : 1;
    }
  }

  List<UserInfo> getLocalUsers();

  void removeLocalUser(String user);

  boolean canRead(MapOfMaps<String, Integer, SerializableGlobType> data);

  interface IdUpdater {
    void update(IntegerField field, Integer lastAllocatedId);
  }

  GlobList getUserData(MutableChangeSet upgradeChangeSetToApply, IdUpdater idUpdater);

  void disconnect();

  static final ServerAccess NULL = new ServerAccess() {
    public boolean createUser(String name, char[] password, boolean autoLogin) throws UserAlreadyExists, IdentificationFailed, PasswordBasedEncryptor.EncryptFail {
      return true;
    }

    public boolean initConnection(String name, char[] password, boolean privateComputer) throws BadPassword, UserNotRegistered {
      return true;
    }

    public void localRegister(byte[] mail, byte[] signature, String activationCode, long jarVersion) {
    }

    public void downloadedVersion(long version) {
    }

    public void applyChanges(ChangeSet changeSet, GlobRepository globRepository) {
    }

    public void takeSnapshot() {
    }

    public LocalInfo connect() {
      return null;
    }

    public MapOfMaps<String, Integer, SerializableGlobType> getServerData() {
      return null;
    }

    public void replaceData(MapOfMaps<String, Integer, SerializableGlobType> data) {
    }

    public void deleteUser(String name, char[] password) {
    }

    public boolean rename(String name, char[] passwd, char[] previousPasswd) throws UserAlreadyExists {
      return false;
    }

    public List<SnapshotInfo> getSnapshotInfos() {
      return null;
    }

    public MapOfMaps<String, Integer, SerializableGlobType> getSnapshotData(SnapshotInfo info, IdUpdater idUpdater) {
      return null;
    }

    public List<UserInfo> getLocalUsers() {
      return null;
    }

    public void removeLocalUser(String user) {
    }

    public boolean canRead(MapOfMaps<String, Integer, SerializableGlobType> data) {
      return false;
    }

    public GlobList getUserData(MutableChangeSet upgradeChangeSetToApply, IdUpdater idUpdater) {
      return GlobList.EMPTY;
    }

    public void disconnect() {
    }
  };

  class LocalInfo{
    private byte[] repoId;
    private byte[] mail;
    private byte[] signature;
    private String activationCode;
    private long count;
    private long downloadVersion;

    public LocalInfo(byte[] repoId, byte[] mail, byte[] signature, String activationCode, long count, long downloadVersion) {
      this.repoId = repoId;
      this.mail = mail;
      this.signature = signature;
      this.activationCode = activationCode;
      this.count = count;
      this.downloadVersion = downloadVersion;
    }

    public byte[] getRepoId() {
      return repoId;
    }

    public byte[] getMail() {
      return mail;
    }

    public byte[] getSignature() {
      return signature;
    }

    public String getActivationCode() {
      return activationCode;
    }

    public long getCount() {
      return count;
    }

    public long getDownloadVersion() {
      return downloadVersion;
    }
  }
}
