package com.budgetview.session;

import com.budgetview.client.exceptions.InvalidActionForState;
import com.budgetview.client.exceptions.UnknownId;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

public interface SessionService {

//  static final int UNKNOWN_ID = 1;
//  static final int INVALID_ACTION = 2;
//  static final int IDENTIFICATION_FAILED = 3;
//  static final int BAD_PASSWORD = 4;
//  static final int USER_ALREADY_EXIST = 5;
//  static final int USER_NOT_REGISTERED = 6;

  void connect(SerializedInput input, SerializedOutput output);

  void createUser(Long sessionId, SerializedInput input, SerializedOutput output);

  void deleteUser(Long sessionId, SerializedInput input, SerializedOutput output);

  void identify(Long sessionId, SerializedInput input, SerializedOutput output);

  void confirmUser(Long sessionId, SerializedInput input, SerializedOutput output) throws UnknownId, InvalidActionForState;

  void register(Long id, SerializedInput input);

  void getUserData(Long sessionId, SerializedInput input, SerializedOutput output) throws UnknownId, InvalidActionForState;

  void getUserId(Long sessionId, SerializedInput input, SerializedOutput output);

  void addUserData(Long sessionId, SerializedInput input, SerializedOutput output) throws UnknownId, InvalidActionForState;

  void disconnect(Long sessionId, SerializedInput serializedInput);

  void takeSnapshot(Long sessionId, SerializedInput serializedInput);

  void restore(Long sessionId, SerializedInput input, SerializedOutput output);

  void getLocalUsers(SerializedOutput output);

  void renameUser(long sessionId, SerializedInput input, SerializedOutput output);

  void localDownload(Long sessionId, SerializedInput input);

  void setLang(Long sessionId, SerializedInput input);

  void getSnapshotInfos(Long sessionId, SerializedInput input, SerializedOutput output);

  void getSnapshotData(Long sessionId, SerializedInput input, SerializedOutput output);

  void hasChanged(Long id, SerializedInput input, SerializedOutput output);
}
