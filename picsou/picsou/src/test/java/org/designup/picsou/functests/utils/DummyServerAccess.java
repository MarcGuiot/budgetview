package org.designup.picsou.functests.utils;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.server.model.SerializableGlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.MapOfMaps;

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

  public void localRegister(byte[] mail, byte[] signature, String activationCode) {
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

