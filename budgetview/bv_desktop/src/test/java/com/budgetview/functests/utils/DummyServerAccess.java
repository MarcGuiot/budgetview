package com.budgetview.functests.utils;

import com.budgetview.client.ServerAccess;
import com.budgetview.client.exceptions.UserAlreadyExists;
import com.budgetview.session.serialization.SerializableGlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.collections.MapOfMaps;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class DummyServerAccess implements ServerAccess {

  private SortedMap<String, Integer> keywordsToCategories = new TreeMap<String, Integer>();
  private int nextId = 1000;

  public DummyServerAccess() {
  }

  public void applyChanges(ChangeSet changeSet, GlobRepository globRepository) {
  }

  public boolean hasChanged() {
    return false;
  }

  public void takeSnapshot() {
  }

  public LocalInfo connect(long version) {
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

  public void addTransaction(GlobList transactions) {
  }

  public GlobList getUserData(MutableChangeSet changeSet, IdUpdater idUpdater) {
    return GlobList.EMPTY;
  }

  public boolean createUser(String name, char[] password, boolean autoLog) {
    return false;
  }

  public boolean initConnection(String name, char[] password, boolean privateComputer) {
    return false;
  }

  public void localRegister(byte[] mail, byte[] signature, String activationCode, long jarVersion) {
  }

  public void downloadedVersion(long version) {
  }

  public void setLang(String lang) {
  }

  public List<UserInfo> getLocalUsers() {
    return null;
  }

  public void removeLocalUser(String user) {
  }

  public boolean canRead(MapOfMaps<String, Integer, SerializableGlobType> data) {
    return false;
  }

  public void disconnect() {
  }
}

