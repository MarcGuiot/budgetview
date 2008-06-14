package org.designup.picsou.client.local;

import org.designup.picsou.client.ClientTransport;
import org.designup.picsou.server.ServerRequestProcessingService;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;

public class LocalClientTransport implements ClientTransport {
  private ServerRequestProcessingService serverRequestProcessingService;

  public LocalClientTransport(Directory serverDirectory) {
    serverRequestProcessingService = serverDirectory.get(ServerRequestProcessingService.class);
  }

  public SerializedInput createUser(byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    serverRequestProcessingService.createUser(SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public SerializedInput identifyUser(byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    serverRequestProcessingService.identify(SerializedInputOutputFactory.init(data), output.getOutput());
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

  public SerializedInput getNextId(Long sessionId, byte[] data) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    serverRequestProcessingService.getNextId(sessionId, SerializedInputOutputFactory.init(data), output.getOutput());
    return output.getInput();
  }

  public void disconnect(Long sessionId, byte[] data) {
    serverRequestProcessingService.disconnect(sessionId,
                                              SerializedInputOutputFactory.init(data));
  }

  public void takeSnapshot(Long sessionId, byte[] data) {
    serverRequestProcessingService.takeSnapshot(sessionId, SerializedInputOutputFactory.init(data));
  }
}
