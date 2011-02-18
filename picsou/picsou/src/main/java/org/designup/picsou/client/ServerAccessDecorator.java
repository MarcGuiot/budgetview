package org.designup.picsou.client;

import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.server.model.SerializableGlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.MapOfMaps;

import java.util.List;

public class ServerAccessDecorator implements ServerAccess {
  private ServerAccess serverAccess;

  public ServerAccessDecorator(ServerAccess serverAccess) {
    this.serverAccess = serverAccess;
  }

  public void setServerAccess(ServerAccess serverAccess) {
    this.serverAccess = serverAccess;
  }

  public boolean createUser(String name, char[] password, boolean autoLogin) throws UserAlreadyExists, IdentificationFailed {
    return serverAccess.createUser(name, password, autoLogin);
  }

  public void deleteUser(String name, char[] password) {
    serverAccess.deleteUser(name, password);
  }

  public boolean rename(String name, char[] passwd, char[] previousPasswd) throws UserAlreadyExists {
    return serverAccess.rename(name, passwd, previousPasswd);
  }

  public boolean initConnection(String name, char[] password, boolean privateComputer) throws IdentificationFailed {
    return serverAccess.initConnection(name, password, privateComputer);
  }

  public void localRegister(byte[] mail, byte[] signature, String activationCode, long jarVersion) {
    serverAccess.localRegister(mail, signature, activationCode, jarVersion);
  }

  public void downloadedVersion(long version) {
    serverAccess.downloadedVersion(version);
  }

  public void applyChanges(ChangeSet changeSet, GlobRepository globRepository) {
    serverAccess.applyChanges(changeSet, globRepository);
  }

  public void takeSnapshot() {
    if (serverAccess != null) {
      serverAccess.takeSnapshot();
    }
  }

  public boolean canRead(MapOfMaps<String, Integer, SerializableGlobType> data) {
    return serverAccess.canRead(data);
  }

  public LocalInfo connect() {
    return serverAccess.connect();
  }

  public MapOfMaps<String, Integer, SerializableGlobType> getServerData() {
    return serverAccess.getServerData();
  }

  public void replaceData(MapOfMaps<String, Integer, SerializableGlobType> data) {
    serverAccess.replaceData(data);
  }

  public List<UserInfo> getLocalUsers() {
    return serverAccess.getLocalUsers();
  }

  public void removeLocalUser(String user) {
    serverAccess.removeLocalUser(user);
  }

  public GlobList getUserData(MutableChangeSet changeSet, IdUpdater idUpdater) {
    return serverAccess.getUserData(changeSet, idUpdater);
  }

  public void disconnect() {
    if (serverAccess != null) {
      serverAccess.disconnect();
      serverAccess = null;
    }
  }
}
