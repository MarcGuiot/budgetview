package org.designup.picsou.client;

import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.designup.picsou.client.exceptions.*;

public interface ClientTransport {
  SerializedInput createUser(byte[] bytes) throws UserAlreadyExists, IdentificationFailed, BadConnection;

  SerializedInput identifyUser(byte[] data) throws BadConnection, BadPassword,
                                                   UserNotRegistered;

  void confirmUser(Long sessionId, byte[] data) throws IdentificationFailed, BadConnection;

  SerializedInput updateUserData(Long sessionId, byte[] bytes);

  SerializedInput getUserData(Long sessionId, byte[] bytes);

  SerializedInput getNextId(Long sessionId, byte[] bytes);

  void disconnect(Long sessionId, byte[] bytes);

  void takeSnapshot(Long sessionId, byte[] bytes);
}
