package org.designup.picsou.client;

import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.delta.MutableChangeSet;
import org.designup.picsou.client.exceptions.BadPassword;
import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.client.exceptions.UserNotRegistered;
import org.designup.picsou.client.http.PasswordBasedEncryptor;

public interface ServerAccess {

  boolean createUser(String name, char[] password)
    throws UserAlreadyExists, IdentificationFailed, PasswordBasedEncryptor.EncryptFail;

  boolean initConnection(String name, char[] password, boolean privateComputer)
    throws BadPassword, UserNotRegistered;

  void applyChanges(ChangeSet changeSet, GlobRepository globRepository);

  void takeSnapshot();

  GlobList getUserData(MutableChangeSet upgradeChangeSetToApply);

  int getNextId(String type, int idCount);

  void disconnect();
}
