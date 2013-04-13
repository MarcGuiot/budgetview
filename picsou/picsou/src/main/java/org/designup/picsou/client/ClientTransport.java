package org.designup.picsou.client;

import org.designup.picsou.client.exceptions.*;
import org.globsframework.utils.serialization.SerializedInput;

public interface ClientTransport {

  SerializedInput connect(long version) throws BadConnection;

  void localRegister(Long sessionId, byte[] privateId, byte[] mail, byte[] signature, String activationCode);

  SerializedInput createUser(Long sessionId, byte[] bytes) throws UserAlreadyExists, IdentificationFailed, BadConnection;

  SerializedInput identifyUser(Long sessionId, byte[] data) throws BadConnection, BadPassword,
                                                                   UserNotRegistered;

  void confirmUser(Long sessionId, byte[] data) throws IdentificationFailed, BadConnection;

  SerializedInput updateUserData(Long sessionId, byte[] bytes);

  SerializedInput getUserData(Long sessionId, byte[] bytes);

  SerializedInput restore(Long sessionId, byte[] data);

  void disconnect(Long sessionId, byte[] bytes);

  void takeSnapshot(Long sessionId, byte[] bytes);

  SerializedInput getLocalUsers();

  void removeLocalUser(String user);

  SerializedInput deleteUser(Long sessionId, byte[] bytes);

  SerializedInput rename(Long sessionId, byte[] bytes);

  void localDownload(Long sessionId, byte[] privateId, long version);

  SerializedInput getSnapshotInfos(Long sessionId, byte[] bytes);

  SerializedInput getSnapshotData(Long sessionId, byte[] data);

  void setLang(Long sessionId, byte[] privateId, String lang);
}
