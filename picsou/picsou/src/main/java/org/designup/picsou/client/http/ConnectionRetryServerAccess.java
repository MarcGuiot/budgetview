package org.designup.picsou.client.http;

import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.delta.MutableChangeSet;
import org.crossbowlabs.globs.utils.exceptions.GlobsException;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.ServerAccessDecorator;
import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.designup.picsou.client.exceptions.UserAlreadyExists;

public class ConnectionRetryServerAccess extends ServerAccessDecorator {
  private String name;
  private char[] password;
  private boolean anonymous;
  private boolean privateComputer;

  public ConnectionRetryServerAccess(ServerAccess serverAccess) {
    super(serverAccess);
  }

  public boolean createUser(String name, char[] password) throws UserAlreadyExists, IdentificationFailed {
    this.name = name;
    this.password = password;
    this.anonymous = anonymous;
    this.privateComputer = privateComputer;
    return super.createUser(name, password);
  }

  public boolean initConnection(String name, char[] password, boolean privateComputer) throws IdentificationFailed {
    this.privateComputer = privateComputer;
    this.anonymous = anonymous;
    this.name = name;
    this.password = password;
    return super.initConnection(this.name, this.password, privateComputer);
  }

  public void applyChanges(ChangeSet changeSet, GlobRepository globRepository) {
    try {
      super.applyChanges(changeSet, globRepository);
    }
    catch (GlobsException e) {
      super.initConnection(name, password, privateComputer);
      super.applyChanges(changeSet, globRepository);
    }
  }

  public void takeSnapshot() {
    try {
      super.takeSnapshot();
    }
    catch (GlobsException e) {
      super.initConnection(name, password, privateComputer);
      super.takeSnapshot();
    }
  }

  public GlobList getUserData(MutableChangeSet changeSet) {
    try {
      return super.getUserData(changeSet);
    }
    catch (GlobsException e) {
      super.initConnection(name, password, privateComputer);
      return super.getUserData(changeSet);
    }
  }

  public int getNextId(String type, int idCount) {
    try {
      return super.getNextId(type, idCount);
    }
    catch (GlobsException e) {
      super.initConnection(name, password, privateComputer);
      return super.getNextId(type, idCount);
    }
  }
}
