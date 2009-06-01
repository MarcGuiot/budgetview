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
import org.globsframework.utils.MapOfMaps;

import java.util.List;

public interface ServerAccess {

  boolean createUser(String name, char[] password)
    throws UserAlreadyExists, IdentificationFailed, PasswordBasedEncryptor.EncryptFail;

  boolean initConnection(String name, char[] password, boolean privateComputer)
    throws BadPassword, UserNotRegistered;

  void localRegister(byte[] mail, byte[] signature, String activationCode);

  void applyChanges(ChangeSet changeSet, GlobRepository globRepository);

  void takeSnapshot();

  boolean connect();

  MapOfMaps<String, Integer, SerializableGlobType> getServerData();

  void replaceData(MapOfMaps<String, Integer, SerializableGlobType> data);

  List<String> getLocalUsers();

  void removeLocalUser(String user);

  interface IdUpdater {
    void update(IntegerField field, Integer lastAllocatedId);
  }

  GlobList getUserData(MutableChangeSet upgradeChangeSetToApply, IdUpdater idUpdater);

  void disconnect();

  static final ServerAccess NULL = new ServerAccess() {
    public boolean createUser(String name, char[] password) throws UserAlreadyExists, IdentificationFailed, PasswordBasedEncryptor.EncryptFail {
      return true;
    }

    public boolean initConnection(String name, char[] password, boolean privateComputer) throws BadPassword, UserNotRegistered {
      return true;
    }

    public void localRegister(byte[] mail, byte[] signature, String activationCode) {
    }

    public void applyChanges(ChangeSet changeSet, GlobRepository globRepository) {
    }

    public void takeSnapshot() {
    }

    public boolean connect() {
      return false;
    }

    public MapOfMaps<String, Integer, SerializableGlobType> getServerData() {
      return null;
    }

    public void replaceData(MapOfMaps<String, Integer, SerializableGlobType> data) {
    }

    public List<String> getLocalUsers() {
      return null;
    }

    public void removeLocalUser(String user) {
    }

    public GlobList getUserData(MutableChangeSet upgradeChangeSetToApply, IdUpdater idUpdater) {
      return GlobList.EMPTY;
    }

    public void disconnect() {
    }
  };
}
