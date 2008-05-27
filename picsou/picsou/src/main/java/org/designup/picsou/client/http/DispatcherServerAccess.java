package org.designup.picsou.client.http;

import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.delta.MutableChangeSet;
import org.crossbowlabs.globs.utils.Log;
import org.crossbowlabs.globs.utils.exceptions.InvalidState;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.exceptions.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

  public void applyChanges(ChangeSet changeSet, GlobRepository globRepository) {
    localServerAccess.applyChanges(changeSet, globRepository);
  }

  public void takeSnapshot() {
    localServerAccess.takeSnapshot();
  }

  public GlobList getUserData(MutableChangeSet changeSet) {
    return localServerAccess.getUserData(changeSet);
  }

  public int getNextId(String type, int idCount) {
    return localServerAccess.getNextId(type, idCount);
  }

  public void disconnect() {
    if (remoteServerAccess != null) {
      remoteServerAccess.disconnect();
    }
    localServerAccess.disconnect();
  }

}
