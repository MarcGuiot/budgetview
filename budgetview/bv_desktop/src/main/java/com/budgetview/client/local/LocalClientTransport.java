package com.budgetview.client.local;

import com.budgetview.client.exceptions.BadConnection;
import com.budgetview.session.SessionService;
import com.budgetview.client.ClientTransport;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class LocalClientTransport implements ClientTransport {
  private SessionService sessionService;

  public LocalClientTransport(Directory serverDirectory) {
    sessionService = serverDirectory.get(SessionService.class);
  }

  public SerializedInput connect(long version) throws BadConnection {
    SerializedInput input = getTrueInByteArray(version);
    SerializedByteArrayOutput byteOutput = new SerializedByteArrayOutput();
    sessionService.connect(input, byteOutput.getOutput());
    return byteOutput.getInput();
  }

  public void localRegister(Long sessionId, byte[] privateId, byte[] mail, byte[] signature, String activationCode) {
    SerializedByteArrayOutput byteOutput = new SerializedByteArrayOutput();
    SerializedOutput output = byteOutput.getOutput();
    output.writeBytes(privateId);
    output.writeBytes(mail);
    output.writeBytes(signature);
    output.writeJavaString(activationCode);
    sessionService.register(sessionId, byteOutput.getInput());
  }

  private SerializedInput getTrueInByteArray(long version) {
    SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
    SerializedOutput output = serializedByteArrayOutput.getOutput();
    output.writeBoolean(true);
    output.write(version);
    return serializedByteArrayOutput.getInput();
  }

  public SerializedInput createUser(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    sessionService.createUser(sessionId, SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public SerializedInput deleteUser(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    sessionService.deleteUser(sessionId, SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public SerializedInput rename(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    sessionService.renameUser(sessionId, SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public void localDownload(Long sessionId, byte[] privateId, long version) {
    SerializedByteArrayOutput byteOutput = new SerializedByteArrayOutput();
    SerializedOutput output = byteOutput.getOutput();
    output.writeBytes(privateId);
    output.write(version);
    sessionService.localDownload(sessionId, byteOutput.getInput());
  }

  public void setLang(Long sessionId, byte[] privateId, String lang) {
    SerializedByteArrayOutput byteOutput = new SerializedByteArrayOutput();
    SerializedOutput output = byteOutput.getOutput();
    output.writeBytes(privateId);
    output.writeUtf8String(lang);
    sessionService.setLang(sessionId, byteOutput.getInput());
  }

  public SerializedInput identifyUser(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    sessionService.identify(sessionId, SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public void confirmUser(Long sessionId, byte[] data) {
    sessionService.confirmUser(sessionId,
                               SerializedInputOutputFactory.init(data),
                               new SerializedByteArrayOutput().getOutput());
  }

  public SerializedInput updateUserData(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    sessionService.addUserData(sessionId,
                               SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public SerializedInput getUserData(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    sessionService.getUserData(sessionId,
                               SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public SerializedInput hasChanged(Long sessionId, byte[] bytes) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    sessionService.hasChanged(sessionId,
                              SerializedInputOutputFactory.init(bytes), output.getOutput());
    return output.getInput();
  }

  public SerializedInput restore(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    sessionService.restore(sessionId, SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public SerializedInput getSnapshotInfos(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    sessionService.getSnapshotInfos(sessionId, SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public SerializedInput getSnapshotData(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    sessionService.getSnapshotData(sessionId, SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public void disconnect(Long sessionId, byte[] data) {
    sessionService.disconnect(sessionId,
                              SerializedInputOutputFactory.init(data));
  }

  public void takeSnapshot(Long sessionId, byte[] data) {
    sessionService.takeSnapshot(sessionId, SerializedInputOutputFactory.init(data));
  }

  public SerializedInput getLocalUsers() {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    sessionService.getLocalUsers(output.getOutput());
    return output.getInput();
  }

  public void removeLocalUser(String user) {
  }
}
