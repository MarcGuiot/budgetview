package org.designup.picsou.client.http;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.exceptions.*;
import org.designup.picsou.server.model.SerializableGlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.Log;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.exceptions.InvalidState;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

public class DispatcherServerAccess implements ServerAccess {
  private ExecutorService pool = Executors.newCachedThreadPool();
  private ServerAccess localServerAccess;
  private ServerAccess remoteServerAccess;

  public DispatcherServerAccess(ServerAccess localServerAccess, ServerAccess remoteServerAccess) {
    this.localServerAccess = localServerAccess;
    this.remoteServerAccess = remoteServerAccess;
  }

  public boolean createUser(String name, char[] password)
    throws UserAlreadyExists, IdentificationFailed, PasswordBasedEncryptor.EncryptFail {
    boolean isRegistered = localServerAccess.createUser(name, password);
    if (remoteServerAccess != null) {
      try {
        try {
          return remoteServerAccess.initConnection(name, password, true);
        }
        catch (UserNotRegistered e) {
          return remoteServerAccess.createUser(name, password);
        }
        catch (BadPassword e) {
          Log.write("A user with named '" + name + "' already exist");
          throw new UserAlreadyExists(e.getMessage());
        }
      }
      catch (UserAlreadyExists userAlreadyExists) {
        throw userAlreadyExists;
      }
      catch (IdentificationFailed identificationFailed) {
        throw identificationFailed;
      }
      catch (PasswordBasedEncryptor.EncryptFail encryptFail) {
        throw encryptFail;
      }
      catch (BadConnection ex) {
        remoteServerAccess = null;
        return isRegistered;
      }
      catch (InvalidState ex) {
        remoteServerAccess = null;
        return isRegistered;
      }
    }
    return isRegistered;
  }

  public boolean initConnection(String name, char[] password, boolean privateComputer) throws IdentificationFailed {
    boolean isRegistered = localServerAccess.initConnection(name, password, false);
    if (remoteServerAccess != null) {
      try {
        return remoteServerAccess.initConnection(name, password, false);
      }
      catch (UserNotRegistered ex) {
        remoteServerAccess.createUser(name, password);
        return remoteServerAccess.initConnection(name, password, false);
      }
      catch (Exception ex) {
        remoteServerAccess = null;
        return isRegistered;
      }
    }
    return isRegistered;
  }

  public boolean connect() {
    boolean validUser = localServerAccess.connect();
    if (remoteServerAccess != null) {
      try {
        boolean remoteValidity = remoteServerAccess.connect();
        if (remoteValidity) {
          return true;
        }
      }
      catch (Exception e) {
      }
    }
    return validUser;
  }

  public MapOfMaps<String, Integer, SerializableGlobType> getServerData() {
    return localServerAccess.getServerData();
  }

  public void replaceData(MapOfMaps<String, Integer, SerializableGlobType> data) {
    localServerAccess.replaceData(data);
  }

  public void localRegister(byte[] mail, byte[] signature, String activationCode) {
    localServerAccess.localRegister(mail, signature, activationCode);
  }

  public void applyChanges(ChangeSet changeSet, GlobRepository globRepository) {
    localServerAccess.applyChanges(changeSet, globRepository);
  }

  public void takeSnapshot() {
    localServerAccess.takeSnapshot();
  }

  public GlobList getUserData(MutableChangeSet changeSet, IdUpdater idUpdater) {
    return localServerAccess.getUserData(changeSet, idUpdater);
  }

  public List<String> getLocalUsers() {
    return localServerAccess.getLocalUsers();
  }

  public void removeLocalUser(String user) {
    localServerAccess.removeLocalUser(user);
  }

  public void disconnect() {
    if (remoteServerAccess != null) {
      remoteServerAccess.disconnect();
    }
    localServerAccess.disconnect();
  }

}
