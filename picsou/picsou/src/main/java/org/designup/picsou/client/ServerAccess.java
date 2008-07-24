package org.designup.picsou.client;

import org.designup.picsou.client.exceptions.BadPassword;
import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.client.exceptions.UserNotRegistered;
import org.designup.picsou.client.http.PasswordBasedEncryptor;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.MutableChangeSet;

public interface ServerAccess {

  boolean createUser(String name, char[] password)
    throws UserAlreadyExists, IdentificationFailed, PasswordBasedEncryptor.EncryptFail;

  boolean initConnection(String name, char[] password, boolean privateComputer)
    throws BadPassword, UserNotRegistered;

  void register(byte[] mail, byte[] signature);

  void applyChanges(ChangeSet changeSet, GlobRepository globRepository);

  void takeSnapshot();

  void connect();

  interface IdUpdate {
    void update(IntegerField field, Integer lastAllocatedId);
  }

  GlobList getUserData(MutableChangeSet upgradeChangeSetToApply, IdUpdate idUpdate);

  void disconnect();
}
