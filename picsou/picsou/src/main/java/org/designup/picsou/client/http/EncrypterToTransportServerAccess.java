package org.designup.picsou.client.http;

import org.designup.picsou.client.ClientTransport;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.exceptions.BadConnection;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.server.model.HiddenServerTypeVisitor;
import org.designup.picsou.server.model.ServerModel;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.remote.SerializedRemoteAccess;
import org.globsframework.utils.exceptions.InvalidState;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.security.SecureRandom;

public class EncrypterToTransportServerAccess implements ServerAccess {
  private ClientTransport clientTransport;
  private String name;
  private Long sessionId;
  private byte[] privateId;
  private PasswordBasedEncryptor passwordBasedEncryptor;
  private boolean notConnected = true;
  static final byte[] salt = {0x54, 0x12, 0x43, 0x65, 0x77, 0x2, 0x79, 0x72};

  public EncrypterToTransportServerAccess(ClientTransport transport) {
    this.clientTransport = transport;
  }

  public boolean createUser(String name, char[] password) throws UserAlreadyExists {
    try {
      this.name = name;
      this.passwordBasedEncryptor = new PasswordBasedEncryptor(salt, password, 20);

      SerializedByteArrayOutput request = new SerializedByteArrayOutput();
      SerializedOutput output = request.getOutput();
      output.writeString(this.name);
      output.writeBytes(generatePassword(password, passwordBasedEncryptor));

      byte[] linkInfo = generateLinkInfo();
      output.writeBytes(linkInfo);
      output.writeBytes(passwordBasedEncryptor.encrypt(linkInfo));

      SerializedInput response = clientTransport.createUser(request.toByteArray());
      sessionId = response.readLong();
      privateId = response.readBytes();
      notConnected = false;
      return response.readBoolean();
    }
    catch (PasswordBasedEncryptor.EncryptFail e) {
      throw e;
    }
    catch (UserAlreadyExists e) {
      throw e;
    }
    catch (BadConnection e) {
      throw e;
    }
    catch (InvalidState e) {
      throw e;
    }
    catch (Exception e) {
      throw new InvalidState(e);
    }
  }

  public boolean initConnection(String name, char[] password, boolean privateComputer) {
    this.name = name;
    this.passwordBasedEncryptor = new PasswordBasedEncryptor(salt, password, 20);

    SerializedByteArrayOutput request = new SerializedByteArrayOutput();
    SerializedOutput output = request.getOutput();

    output.writeString(this.name);
    output.writeBytes(generatePassword(password, passwordBasedEncryptor));

    SerializedInput response = clientTransport.identifyUser(request.toByteArray());
    sessionId = response.readLong();
    privateId = response.readBytes();

    SerializedByteArrayOutput confirmation = new SerializedByteArrayOutput();
    confirmation.getOutput().writeBytes(privateId);
    confirmation.getOutput().writeBytes(passwordBasedEncryptor.encrypt(response.readBytes()));
    Boolean isRegistered = response.readBoolean();
    clientTransport.confirmUser(sessionId, confirmation.toByteArray());
    notConnected = false;
    return isRegistered;
  }

  private byte[] generateLinkInfo() {
    SecureRandom secureRandom = new SecureRandom();
    byte[] linkInfo = new byte[128];
    secureRandom.nextBytes(linkInfo);
    return linkInfo;
  }

  private byte[] generatePassword(char[] name, PasswordBasedEncryptor passwordEncryptor) {
    byte[] tab = new byte[name.length];
    try {
      int i = 0;
      for (char c : name) {
        tab[i] = (byte)c;
        i++;
      }
      return passwordEncryptor.encrypt(tab);
    }
    finally {
      for (int i = 0; i < tab.length; i++) {
        tab[i] = 0;
      }
    }
  }

  private void checkConnected() {
    if (notConnected) {
      throw new InvalidState("not connected");
    }
  }

  public void applyChanges(ChangeSet changeSet, final GlobRepository globRepository) {
    SerializedByteArrayOutput request = new SerializedByteArrayOutput();
    final SerializedRemoteAccess.ChangeVisitor globChangeVisitor =
      SerializedRemoteAccess.getChangeVisitor(request.getOutput());
    changeSet.safeVisit(new ChangeSetSerializerVisitor(globChangeVisitor, globRepository, passwordBasedEncryptor));
    if (request.size() != 0) {
      globChangeVisitor.complete();
      updateUserData(request.toByteArray());
    }
  }

  public void takeSnapshot() {
    if (notConnected) {
      return;
    }
    checkConnected();
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    clientTransport.takeSnapshot(sessionId, outputStream.toByteArray());
  }

  private void updateUserData(byte[] hiddenTransactions) {
    checkConnected();
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    outputStream.getOutput().writeBytes(hiddenTransactions);
    clientTransport.updateUserData(sessionId, outputStream.toByteArray());
  }

  public GlobList getUserData(MutableChangeSet changeSet) {
    checkConnected();
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    SerializedInput input = clientTransport.getUserData(sessionId, outputStream.toByteArray());
    int globCount = input.readNotNullInt();
    HiddenServerTypeVisitorDecoder typeVisitorDecoder =
      new HiddenServerTypeVisitorDecoder(passwordBasedEncryptor, globCount, changeSet);
    while (globCount != 0) {
      Glob glob = input.readGlob(ServerModel.get());
      typeVisitorDecoder.set(glob);
      HiddenServerTypeVisitor.Visitor.safeVisit(glob.getType(), typeVisitorDecoder);
      globCount--;
    }
    return typeVisitorDecoder.getGlobs();
  }

  public int getNextId(String type, int idCount) {
    checkConnected();

    SerializedByteArrayOutput request = new SerializedByteArrayOutput();
    request.getOutput().writeBytes(privateId);
    request.getOutput().writeString(type);
    request.getOutput().write(idCount);

    SerializedInput response = clientTransport.getNextId(sessionId, request.toByteArray());
    return response.readInteger();
  }

  public void disconnect() {
    if (notConnected) {
      return;
    }
    SerializedByteArrayOutput request = new SerializedByteArrayOutput();
    request.getOutput().writeBytes(privateId);
    clientTransport.disconnect(sessionId, request.toByteArray());
  }

}
