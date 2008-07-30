package org.designup.picsou.client;

import org.designup.picsou.client.exceptions.*;
import org.globsframework.utils.serialization.SerializedInput;

public interface ClientTransport {

  SerializedInput connect() throws BadConnection;

  void register(Long sessionId, byte[] privateId, byte[] mail, byte[] signature, String activationCode);

  SerializedInput createUser(Long sessionId, byte[] bytes) throws UserAlreadyExists, IdentificationFailed, BadConnection;

  SerializedInput identifyUser(Long sessionId, byte[] data) throws BadConnection, BadPassword,
                                                                   UserNotRegistered;

  void confirmUser(Long sessionId, byte[] data) throws IdentificationFailed, BadConnection;

  SerializedInput updateUserData(Long sessionId, byte[] bytes);

  SerializedInput getUserData(Long sessionId, byte[] bytes);

  void disconnect(Long sessionId, byte[] bytes);

  void takeSnapshot(Long sessionId, byte[] bytes);
}
