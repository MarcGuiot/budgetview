package org.designup.picsou.server;

import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;
import org.designup.picsou.client.exceptions.InvalidActionForState;
import org.designup.picsou.client.exceptions.UnknownId;

public interface ServerRequestProcessingService {
//  static final int UNKNOWN_ID = 1;
//  static final int INVALID_ACTION = 2;
//  static final int IDENTIFICATION_FAILED = 3;
//  static final int BAD_PASSWORD = 4;
//  static final int USER_ALREADY_EXIST = 5;
//  static final int USER_NOT_REGISTERED = 6;

  void createUser(SerializedInput input, SerializedOutput output);

  void confirmUser(Long sessionId, SerializedInput input, SerializedOutput output) throws UnknownId, InvalidActionForState;

  void identify(SerializedInput input, SerializedOutput output);

  void getUserData(Long sessionId, SerializedInput input, SerializedOutput output) throws UnknownId, InvalidActionForState;

  void addUserData(Long sessionId, SerializedInput input, SerializedOutput output) throws UnknownId, InvalidActionForState;

  void getNextId(Long sessionId, SerializedInput serializedInput, SerializedOutput output) throws UnknownId, InvalidActionForState;

  void disconnect(Long sessionId, SerializedInput serializedInput);

  void takeSnapshot(Long sessionId, SerializedInput serializedInput);
}
