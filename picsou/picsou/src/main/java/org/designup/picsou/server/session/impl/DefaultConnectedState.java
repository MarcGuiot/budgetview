package org.designup.picsou.server.session.impl;

import org.designup.picsou.server.session.ConnectedState;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

public class DefaultConnectedState extends AbstractSessionState implements ConnectedState {
  private Integer userId;
  private Persistence persistence;

  public DefaultConnectedState(Persistence persistence, Integer userId,
                               DefaultSessionService defaultSessionService, Long sessionId,
                               byte[] privateId) {
    super(defaultSessionService, sessionId, privateId);
    this.persistence = persistence;
    this.userId = userId;
    defaultSessionService.register(sessionId, this);
  }

  public void getUserData(SerializedInput input, SerializedOutput output) {
    lastAccess();
    checkPrivateId(input);
    persistence.getData(output, userId);
  }

  public void updateData(SerializedInput input, SerializedOutput output) {
    lastAccess();
    checkPrivateId(input);
    persistence.updateData(input, output, userId);
  }

  public void getUserId(SerializedInput input, SerializedOutput output) {
    lastAccess();
    checkPrivateId(input);
    output.writeInteger(userId);
  }

  public void takeSnapshot(SerializedInput input) {
    lastAccess();
    checkPrivateId(input);
    persistence.takeSnapshot(userId);
  }

  public void restore(SerializedInput input, SerializedOutput output) {
    lastAccess();
    checkPrivateId(input);
    output.writeBoolean(persistence.restore(input, userId));
  }

  public void register(SerializedInput input) {
    lastAccess();
    checkPrivateId(input);
    persistence.register(input.readBytes(), input.readBytes(), input.readJavaString());
  }

  public String getStateName() {
    return "Connected";
  }

  public ConnectedState connected() {
    return this;
  }

  public void disconnect(SerializedInput input) {
    super.disconnect(input);
    persistence.close(userId);
  }
}
