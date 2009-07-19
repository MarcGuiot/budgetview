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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EncrypterToTransportServerAccess implements ServerAccess {
  private ClientTransport clientTransport;
  private Directory directory;
  private String name;
  private Long sessionId;
  private byte[] privateId;
  private boolean notConnected = true;
  static public final byte[] salt = {0x54, 0x12, 0x43, 0x65, 0x77, 0x2, 0x79, 0x72};
  private GlobModel globModel;
  private ConfigService configService;
  public static int count = 20;

  public EncrypterToTransportServerAccess(ClientTransport transport, Directory directory) {
    this.clientTransport = transport;
    this.directory = directory;
    globModel = directory.get(GlobModel.class);
    configService = directory.get(ConfigService.class);
  }

  public boolean connect() {
    SerializedInput response = clientTransport.connect();
    boolean isValidUser = false;
    if (response.readBoolean()) {
      byte[] repoId = response.readBytes();
      byte[] mail = response.readBytes();
      byte[] signature = response.readBytes();
      String activationCode = response.readJavaString();
      long count = response.readNotNullLong();
      isValidUser = configService.update(repoId, count, mail, signature, activationCode);
    }
    sessionId = response.readLong();
    privateId = response.readBytes();
    return isValidUser;
  }

  public boolean createUser(String name, char[] password) throws UserAlreadyExists {
    try {
      this.name = name;

      PasswordBasedEncryptor passwordBasedEncryptor = new MD5PasswordBasedEncryptor(salt, password, count);
      RedirectPasswordBasedEncryptor encryptor = (RedirectPasswordBasedEncryptor)directory.get(PasswordBasedEncryptor.class);
      encryptor.setPasswordBasedEncryptor(passwordBasedEncryptor);

      SerializedByteArrayOutput request = new SerializedByteArrayOutput();
      SerializedOutput output = request.getOutput();
      output.writeUtf8String(this.name);
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

    PasswordBasedEncryptor passwordBasedEncryptor = new MD5PasswordBasedEncryptor(salt, password, count);
    RedirectPasswordBasedEncryptor encryptor = (RedirectPasswordBasedEncryptor)directory.get(PasswordBasedEncryptor.class);
    encryptor.setPasswordBasedEncryptor(passwordBasedEncryptor);

    SerializedByteArrayOutput request = new SerializedByteArrayOutput();
    SerializedOutput output = request.getOutput();

    output.writeUtf8String(this.name);
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

  public void localRegister(byte[] mail, byte[] signature, String activationCode) {
    clientTransport.localRegister(sessionId, privateId, mail, signature, activationCode);
  }

  public List<String> getLocalUsers() {
    SerializedInput input = clientTransport.getLocalUsers();
    int size = input.readNotNullInt();
    List<String> users = new ArrayList<String>(size);
    for (int i = 0; i < size; i++) {
      users.add(input.readJavaString());
    }
    return users;
  }

  public void removeLocalUser(String user) {
    clientTransport.removeLocalUser(user);
  }

  public boolean canRead(MapOfMaps<String, Integer, SerializableGlobType> data) {
    return false;
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
    PasswordBasedEncryptor passwordBasedEncryptor = directory.get(PasswordBasedEncryptor.class);
    ChangeSetSerializerVisitor serializerVisitor = new ChangeSetSerializerVisitor(passwordBasedEncryptor, repository);
    changeSet.safeVisit(serializerVisitor);
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    MultiMap<String, ServerDelta> stringDeltaGlobMultiMap = serializerVisitor.getSerializableGlob();
    SerializableDeltaGlobSerializer.serialize(outputStream.getOutput(), stringDeltaGlobMultiMap);
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

  public MapOfMaps<String, Integer, SerializableGlobType> getServerData() {
    checkConnected();
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    SerializedInput input = clientTransport.getUserData(sessionId, outputStream.toByteArray());
    MapOfMaps<String, Integer, SerializableGlobType> data = new MapOfMaps<String, Integer, SerializableGlobType>();
    SerializableGlobSerializer.deserialize(input, data);
    return data;
  }

  public void replaceData(MapOfMaps<String, Integer, SerializableGlobType> data) {
    checkConnected();
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    SerializableGlobSerializer.serialize(outputStream.getOutput(), data);

    clientTransport.restore(sessionId, outputStream.toByteArray());
  }

  public GlobList getUserData(MutableChangeSet changeSet, IdUpdater idUpdater) {
    checkConnected();
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    SerializedInput input = clientTransport.getUserData(sessionId, outputStream.toByteArray());
    MapOfMaps<String, Integer, SerializableGlobType> data = new MapOfMaps<String, Integer, SerializableGlobType>();
    SerializableGlobSerializer.deserialize(input, data);
    return decrypt(idUpdater, data, directory.get(PasswordBasedEncryptor.class), globModel);
  }

  public static GlobList decrypt(IdUpdater idUpdater, MapOfMaps<String, Integer, SerializableGlobType> data,
                                 final PasswordBasedEncryptor passwordBasedEncryptor, final GlobModel globModel) {
    GlobList result = new GlobList(data.size());

    for (String globTypeName : data.keys()) {
      GlobType globType;
      if (globTypeName.equals("currenMonth")) {
        globType = globModel.getType("currentMonth");
      }
      else {
        globType = globModel.getType(globTypeName);
      }
      PicsouGlobSerializer globSerializer =
        globType.getProperty(SerializationManager.SERIALIZATION_PROPERTY);
      if (globSerializer == null) {
        throw new RuntimeException("missing serialializer for " + globTypeName);
      }
      IntegerField field = (IntegerField)globType.getKeyFields()[0];
      Integer id;
      Integer maxId = 0;
      for (Map.Entry<Integer, SerializableGlobType> globEntry : data.get(globTypeName).entrySet()) {
        id = globEntry.getKey();
        GlobBuilder builder = GlobBuilder.init(globType).setValue(field, id);
        SerializableGlobType glob = globEntry.getValue();
        globSerializer.deserializeData(glob.getVersion(),
                                       builder,
                                       passwordBasedEncryptor.decrypt(glob.getData()),
                                       id);
        result.add(builder.get());
        maxId = Math.max(maxId, id);
      }
      idUpdater.update(field, maxId);
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
