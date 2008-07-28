package org.designup.picsou.client.http;

import org.designup.picsou.client.ClientTransport;
import org.designup.picsou.client.SerializableDeltaGlobSerializer;
import org.designup.picsou.client.SerializableGlobSerializer;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.exceptions.BadConnection;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.model.ServerDelta;
import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.designup.picsou.server.serialization.SerializationManager;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.security.SecureRandom;
import java.util.Map;

public class EncrypterToTransportServerAccess implements ServerAccess {
  private ClientTransport clientTransport;
  private String name;
  private Long sessionId;
  private byte[] privateId;
  private PasswordBasedEncryptor passwordBasedEncryptor;
  private boolean notConnected = true;
  static final byte[] salt = {0x54, 0x12, 0x43, 0x65, 0x77, 0x2, 0x79, 0x72};
  private GlobModel globModel;
  private ConfigService configService;

  public EncrypterToTransportServerAccess(ClientTransport transport, Directory directory) {
    this.clientTransport = transport;
    globModel = directory.get(GlobModel.class);
    configService = directory.get(ConfigService.class);
  }

  public void connect() {
    SerializedInput response = clientTransport.connect();
    if (response.readBoolean()) {
      byte[] repoId = response.readBytes();
      byte[] mail = response.readBytes();
      byte[] signature = response.readBytes();
      String activationCode = response.readString();
      long count = response.readNotNullLong();
      configService.update(repoId, count, mail, signature, activationCode);
    }
    sessionId = response.readLong();
    privateId = response.readBytes();
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

      SerializedInput response = clientTransport.createUser(sessionId, request.toByteArray());
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

    SerializedInput response = clientTransport.identifyUser(sessionId, request.toByteArray());

    SerializedByteArrayOutput confirmation = new SerializedByteArrayOutput();
    confirmation.getOutput().writeBytes(privateId);
    confirmation.getOutput().writeBytes(passwordBasedEncryptor.encrypt(response.readBytes()));
    Boolean isRegistered = response.readBoolean();
    clientTransport.confirmUser(sessionId, confirmation.toByteArray());
    notConnected = false;
    return isRegistered;
  }

  public void register(byte[] mail, byte[] signature) {
    clientTransport.register(sessionId, privateId, mail, signature);
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

  public void applyChanges(ChangeSet changeSet, final GlobRepository repository) {
    ChangeSetSerializerVisitor serializerVisitor = new ChangeSetSerializerVisitor(passwordBasedEncryptor, repository);
    changeSet.safeVisit(serializerVisitor);
    SerializableDeltaGlobSerializer deltaGlobSerializer = new SerializableDeltaGlobSerializer();
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    MultiMap<String, ServerDelta> stringDeltaGlobMultiMap = serializerVisitor.getSerializableGlob();
    deltaGlobSerializer.serialize(outputStream.getOutput(), stringDeltaGlobMultiMap);
    if (stringDeltaGlobMultiMap.size() != 0) {
      updateUserData(outputStream.toByteArray());
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

  private void updateUserData(byte[] bytes) {
    checkConnected();
    clientTransport.updateUserData(sessionId, bytes);
  }

  public GlobList getUserData(MutableChangeSet changeSet, IdUpdate idUpdate) {
    checkConnected();
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    SerializedInput input = clientTransport.getUserData(sessionId, outputStream.toByteArray());
    return deserialize(idUpdate, input);
  }

  private GlobList deserialize(IdUpdate idUpdate, SerializedInput input) {
    SerializableGlobSerializer serializableGlobSerializer = new SerializableGlobSerializer();
    MapOfMaps<String, Integer, SerializableGlobType> data = new MapOfMaps<String, Integer, SerializableGlobType>();
    serializableGlobSerializer.deserialize(input, data);
    GlobList result = new GlobList(data.size());
    for (String globTypeName : data.keys()) {
      GlobType globType = globModel.getType(globTypeName);
      PicsouGlobSerializer globSerializer =
        globType.getProperty(SerializationManager.SERIALIZATION_PROPERTY);
      if (globSerializer == null) {
        throw new RuntimeException("missing serialializer for " + globTypeName);
      }
      IntegerField field = (IntegerField)globType.getKeyFields().get(0);
      Integer id;
      Integer maxId = 0;
      for (Map.Entry<Integer, SerializableGlobType> globEntry : data.get(globTypeName).entrySet()) {
        id = globEntry.getKey();
        GlobBuilder builder = GlobBuilder.init(globType).setValue(field, id);
        SerializableGlobType glob = globEntry.getValue();
        globSerializer.deserializeData(glob.getVersion(), builder,
                                       passwordBasedEncryptor.decrypt(glob.getData()));
        result.add(builder.get());
        maxId = Math.max(maxId, id);
      }
      idUpdate.update(field, maxId);
    }
    return result;
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
