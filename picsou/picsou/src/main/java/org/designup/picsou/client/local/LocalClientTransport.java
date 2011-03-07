package org.designup.picsou.client.local;

import org.designup.picsou.client.ClientTransport;
import org.designup.picsou.client.exceptions.BadConnection;
import org.designup.picsou.server.ServerRequestProcessingService;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class LocalClientTransport implements ClientTransport {
  private ServerRequestProcessingService serverRequestProcessingService;

  public LocalClientTransport(Directory serverDirectory) {
    serverRequestProcessingService = serverDirectory.get(ServerRequestProcessingService.class);
  }

  public SerializedInput connect() throws BadConnection {
    SerializedInput input = getTrueInByteArray();
    SerializedByteArrayOutput byteOutput = new SerializedByteArrayOutput();
    serverRequestProcessingService.connect(input, byteOutput.getOutput());
    return byteOutput.getInput();
  }

  public void localRegister(Long sessionId, byte[] privateId, byte[] mail, byte[] signature, String activationCode) {
    SerializedByteArrayOutput byteOutput = new SerializedByteArrayOutput();
    SerializedOutput output = byteOutput.getOutput();
    output.writeBytes(privateId);
    output.writeBytes(mail);
    output.writeBytes(signature);
    output.writeJavaString(activationCode);
    serverRequestProcessingService.register(sessionId, byteOutput.getInput());
  }

  private SerializedInput getTrueInByteArray() {
    SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
    SerializedOutput output = serializedByteArrayOutput.getOutput();
    output.writeBoolean(true);
    return serializedByteArrayOutput.getInput();
  }

  public SerializedInput createUser(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    serverRequestProcessingService.createUser(sessionId, SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public SerializedInput deleteUser(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    serverRequestProcessingService.deleteUser(sessionId, SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public SerializedInput rename(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    serverRequestProcessingService.renameUser(sessionId, SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public void localDownload(Long sessionId, byte[] privateId, long version) {
    SerializedByteArrayOutput byteOutput = new SerializedByteArrayOutput();
    SerializedOutput output = byteOutput.getOutput();
    output.writeBytes(privateId);
    output.write(version);
    serverRequestProcessingService.localDownload(sessionId, byteOutput.getInput());
  }

  public SerializedInput identifyUser(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    serverRequestProcessingService.identify(sessionId, SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public void confirmUser(Long sessionId, byte[] data) {
    serverRequestProcessingService.confirmUser(sessionId,
                                               SerializedInputOutputFactory.init(data),
                                               new SerializedByteArrayOutput().getOutput());
  }

  public SerializedInput updateUserData(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    serverRequestProcessingService.addUserData(sessionId,
                                               SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public SerializedInput getUserData(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    serverRequestProcessingService.getUserData(sessionId,
                                               SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public SerializedInput restore(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    serverRequestProcessingService.restore(sessionId, SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public SerializedInput getSnapshotInfos(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    serverRequestProcessingService.getSnapshotInfos(sessionId, SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public SerializedInput getSnapshotData(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    serverRequestProcessingService.getSnapshotData(sessionId, SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public void disconnect(Long sessionId, byte[] data) {
    serverRequestProcessingService.disconnect(sessionId,
                                              SerializedInputOutputFactory.init(data));
  }

  public void takeSnapshot(Long sessionId, byte[] data) {
    serverRequestProcessingService.takeSnapshot(sessionId, SerializedInputOutputFactory.init(data));
  }

  public SerializedInput getLocalUsers() {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    serverRequestProcessingService.getLocalUsers(output.getOutput());
    return output.getInput();
  }

  public void removeLocalUser(String user) {
  }
}
