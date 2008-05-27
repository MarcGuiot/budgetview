package org.designup.picsou.client;

import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.delta.MutableChangeSet;
import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.designup.picsou.client.exceptions.UserAlreadyExists;

public class ServerAccessDecorator implements ServerAccess {
  private ServerAccess serverAccess;

  public ServerAccessDecorator(ServerAccess serverAccess) {
    this.serverAccess = serverAccess;
  }

  public boolean createUser(String name, char[] password) throws UserAlreadyExists, IdentificationFailed {
    return serverAccess.createUser(name, password);
  }

  public boolean initConnection(String name, char[] password, boolean privateComputer) throws IdentificationFailed {
    return serverAccess.initConnection(name, password, privateComputer);
  }

  public void applyChanges(ChangeSet changeSet, GlobRepository globRepository) {
    serverAccess.applyChanges(changeSet, globRepository);
  }

  public void takeSnapshot() {
    serverAccess.takeSnapshot();
  }

    public GlobList getUserData(MutableChangeSet changeSet) {
    return serverAccess.getUserData(changeSet);
  }

  public int getNextId(String type, int idCount) {
    return serverAccess.getNextId(type, idCount);
  }

  public void disconnect() {
    serverAccess.disconnect();
  }
}
