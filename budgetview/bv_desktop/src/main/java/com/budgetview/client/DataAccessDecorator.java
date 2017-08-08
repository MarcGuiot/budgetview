package com.budgetview.client;

import com.budgetview.client.exceptions.IdentificationFailed;
import com.budgetview.client.exceptions.UserAlreadyExists;
import com.budgetview.session.serialization.SerializedGlob;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.collections.MapOfMaps;

import java.util.List;

public class DataAccessDecorator implements DataAccess {
  private DataAccess dataAccess;

  public DataAccessDecorator(DataAccess dataAccess) {
    this.dataAccess = dataAccess;
  }

  public void setDataAccess(DataAccess dataAccess) {
    this.dataAccess = dataAccess;
  }

  public boolean createUser(String name, char[] password, boolean autoLogin) throws UserAlreadyExists, IdentificationFailed {
    return dataAccess.createUser(name, password, autoLogin);
  }

  public void deleteUser(String name, char[] password) {
    dataAccess.deleteUser(name, password);
  }

  public boolean rename(String name, char[] passwd, char[] previousPasswd) throws UserAlreadyExists {
    return dataAccess.rename(name, passwd, previousPasswd);
  }

  public List<SnapshotInfo> getSnapshotInfos() {
    return dataAccess.getSnapshotInfos();
  }

  public MapOfMaps<String, Integer, SerializedGlob> getSnapshotData(SnapshotInfo info, IdUpdater idUpdater) {
    return dataAccess.getSnapshotData(info, null);
  }

  public boolean initConnection(String name, char[] password, boolean privateComputer) throws IdentificationFailed {
    return dataAccess.initConnection(name, password, privateComputer);
  }

  public void localRegister(byte[] mail, byte[] signature, String activationCode, long jarVersion) {
    dataAccess.localRegister(mail, signature, activationCode, jarVersion);
  }

  public void downloadedVersion(long version) {
    dataAccess.downloadedVersion(version);
  }

  public void setLang(String lang) {
    dataAccess.setLang(lang);
  }

  public void applyChanges(ChangeSet changeSet, GlobRepository globRepository) {
    dataAccess.applyChanges(changeSet, globRepository);
  }

  public boolean hasChanged() {
    return dataAccess != null && dataAccess.hasChanged();
  }

  public void takeSnapshot() {
    if (dataAccess != null) {
      dataAccess.takeSnapshot();
    }
  }

  public boolean canRead(MapOfMaps<String, Integer, SerializedGlob> data) {
    return dataAccess.canRead(data);
  }

  public LocalInfo connect(long version) {
    return dataAccess.connect(version);
  }

  public MapOfMaps<String, Integer, SerializedGlob> getServerData() {
    return dataAccess.getServerData();
  }

  public void replaceData(MapOfMaps<String, Integer, SerializedGlob> data) {
    dataAccess.replaceData(data);
  }

  public List<UserInfo> getLocalUsers() {
    return dataAccess.getLocalUsers();
  }

  public void removeLocalUser(String user) {
    dataAccess.removeLocalUser(user);
  }

  public GlobList getUserData(MutableChangeSet changeSet, IdUpdater idUpdater) {
    return dataAccess.getUserData(changeSet, idUpdater);
  }

  public void disconnect() {
    if (dataAccess != null) {
      dataAccess.disconnect();
      dataAccess = null;
    }
  }
}
