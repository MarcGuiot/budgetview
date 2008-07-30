package org.designup.picsou.client.http;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.ServerAccessDecorator;
import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.exceptions.GlobsException;

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

  public void register(byte[] mail, byte[] signature, String activationCode) {
    try {
      super.register(mail, signature, activationCode);
    }
    catch (GlobsException e) {
      super.initConnection(name, password, privateComputer);
      super.register(mail, signature, activationCode);
    }
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

  public GlobList getUserData(MutableChangeSet changeSet, IdUpdate idUpdate) {
    try {
      return super.getUserData(changeSet, idUpdate);
    }
    catch (GlobsException e) {
      super.initConnection(name, password, privateComputer);
      return super.getUserData(changeSet, idUpdate);
    }
  }

}
