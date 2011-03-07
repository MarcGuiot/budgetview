package org.designup.picsou.server.session.impl;

import org.designup.picsou.server.session.ConnectedState;
import org.designup.picsou.server.session.Persistence;
import org.designup.picsou.server.persistence.prevayler.AccountDataManager;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.List;

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

  public void getSnapshotInfos(SerializedInput input, SerializedOutput output) {
    lastAccess();
    checkPrivateId(input);
    List<AccountDataManager.SnapshotInfo> snapshotInfoList = persistence.getSnapshotInfos(userId);
    output.write(snapshotInfoList.size());
    for (AccountDataManager.SnapshotInfo info : snapshotInfoList) {
      output.write(info.timestamp);
      output.write(info.version);
      output.writeUtf8String(info.fileName);
      output.writeJavaString(info.password == null ? null : new String(info.password));
    }
  }

  public void getSnapshotData(SerializedInput input, SerializedOutput output) {
    lastAccess();
    checkPrivateId(input);
    String file = input.readUtf8String();
    persistence.getSnapshotData(file, output, userId);
  }

  public void restore(SerializedInput input, SerializedOutput output) {
    lastAccess();
    checkPrivateId(input);
    output.writeBoolean(persistence.restore(input, userId));
  }

  public void renameUser(SerializedInput input, SerializedOutput output) {
    lastAccess();
    checkPrivateId(input);
    String newName = input.readUtf8String();
    String oldName = input.readUtf8String();
    boolean autoLog = input.readBoolean();
    byte[] encryptedPassword = input.readBytes();
    byte[] oldLinkInfo = input.readBytes();
    byte[] oldEncryptedLinkInfo = input.readBytes();
    byte[] linkInfo = input.readBytes();
    byte[] encryptedLinkInfo = input.readBytes();
    Integer newUserId = persistence.renameUser(newName, oldName, autoLog, encryptedPassword,
                                               oldLinkInfo, oldEncryptedLinkInfo,
                                               linkInfo, encryptedLinkInfo, userId, input);
    if (newUserId != null) {
      output.writeBoolean(Boolean.TRUE);
      userId = newUserId;
    }
    else {
      output.writeBoolean(Boolean.FALSE);
    }
  }
  
  public void localDownload(SerializedInput input) {
    lastAccess();
    checkPrivateId(input);
    long version = input.readNotNullLong();
    persistence.setDownloadedVersion(version);
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
