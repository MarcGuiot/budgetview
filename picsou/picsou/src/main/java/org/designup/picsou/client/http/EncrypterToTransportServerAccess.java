package org.designup.picsou.client.http;

import org.designup.picsou.client.ClientTransport;
import org.designup.picsou.client.SerializableDeltaGlobSerializer;
import org.designup.picsou.client.SerializableGlobSerializer;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.exceptions.BadConnection;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.client.exceptions.BadPassword;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.model.ServerDelta;
import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.designup.picsou.server.serialization.SerializationManager;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.collections.MapOfMaps;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.security.SecureRandom;
import java.util.*;

public class EncrypterToTransportServerAccess implements ServerAccess {
  private ClientTransport clientTransport;
  private Directory directory;
  private String name;
  private Long sessionId;
  private byte[] privateId;
  private boolean notConnected = true;
  static public final byte[] salt = {0x54, 0x12, 0x43, 0x65, 0x77, 0x2, 0x79, 0x72};
  private GlobModel globModel;
  public static int count = 20;
  private static final byte[] SOME_TEXT_TO_CHECK = "some text to check".getBytes();
  private byte[] linkInfo;

  public EncrypterToTransportServerAccess(ClientTransport transport, Directory directory) {
    this.clientTransport = transport;
    this.directory = directory;
    globModel = directory.get(GlobModel.class);
  }

  public LocalInfo connect() {
    SerializedInput response = clientTransport.connect();
    try {
      if (response.readBoolean()) {
        byte[] repoId = response.readBytes();
        byte[] mail = response.readBytes();
        byte[] signature = response.readBytes();
        String activationCode = response.readJavaString();
        long count = response.readNotNullLong();
        long downloadVersion = response.readNotNullLong();
        sessionId = response.readLong();
        privateId = response.readBytes();
        return new LocalInfo(repoId, mail, signature, activationCode, count, downloadVersion);
      }
      sessionId = response.readLong();
      privateId = response.readBytes();
    }
    finally {
      response.close();
    }
    return null;
  }

  public boolean createUser(String name, char[] password, boolean autoLogin) throws UserAlreadyExists {
    SerializedInput response = null;
    try {
      this.name = name;
      PasswordBasedEncryptor passwordBasedEncryptor = new MD5PasswordBasedEncryptor(salt, password, count);
      RedirectPasswordBasedEncryptor encryptor = (RedirectPasswordBasedEncryptor)directory.get(PasswordBasedEncryptor.class);
      encryptor.setPasswordBasedEncryptor(passwordBasedEncryptor);

      SerializedByteArrayOutput request = new SerializedByteArrayOutput();
      SerializedOutput output = request.getOutput();
      output.writeUtf8String(this.name);
      output.write(autoLogin);
      output.writeBytes(cryptPassword(password, passwordBasedEncryptor));

      linkInfo = generateLinkInfo();
      output.writeBytes(linkInfo);
      output.writeBytes(passwordBasedEncryptor.encrypt(linkInfo));

      response = clientTransport.createUser(sessionId, request.toByteArray());
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
    finally {
      if (response != null){
        response.close();
      }
    }
  }

  public void deleteUser(String name, char[] password) {
    PasswordBasedEncryptor passwordBasedEncryptor = new MD5PasswordBasedEncryptor(salt, password, count);
    RedirectPasswordBasedEncryptor encryptor = (RedirectPasswordBasedEncryptor)directory.get(PasswordBasedEncryptor.class);
    encryptor.setPasswordBasedEncryptor(passwordBasedEncryptor);

    linkInfo = requestLinkInfo(name, password, passwordBasedEncryptor);

    SerializedByteArrayOutput confirmation = new SerializedByteArrayOutput();
    confirmation.getOutput().writeBytes(privateId);
    confirmation.getOutput().writeBytes(passwordBasedEncryptor.encrypt(linkInfo));
    clientTransport.deleteUser(sessionId, confirmation.toByteArray()).close();
  }

  private byte[] requestLinkInfo(String name, char[] password, PasswordBasedEncryptor passwordBasedEncryptor) {
    SerializedByteArrayOutput request = new SerializedByteArrayOutput();
    SerializedOutput output = request.getOutput();
    output.writeUtf8String(name);
    output.writeBytes(cryptPassword(password, passwordBasedEncryptor));

    SerializedInput response = clientTransport.identifyUser(sessionId, request.toByteArray());
    byte[] bytes = response.readBytes();
    response.close();
    return bytes;
  }

  public boolean initConnection(String name, char[] password, boolean privateComputer) {
    this.name = name;

    PasswordBasedEncryptor passwordBasedEncryptor = new MD5PasswordBasedEncryptor(salt, password, count);
    RedirectPasswordBasedEncryptor encryptor = (RedirectPasswordBasedEncryptor)directory.get(PasswordBasedEncryptor.class);
    encryptor.setPasswordBasedEncryptor(passwordBasedEncryptor);

    SerializedByteArrayOutput request = new SerializedByteArrayOutput();
    SerializedOutput output = request.getOutput();

    output.writeUtf8String(this.name);
    output.writeBytes(cryptPassword(password, passwordBasedEncryptor));

    SerializedInput response = clientTransport.identifyUser(sessionId, request.toByteArray());

    SerializedByteArrayOutput confirmation = new SerializedByteArrayOutput();
    confirmation.getOutput().writeBytes(privateId);
    linkInfo = response.readBytes();
    confirmation.getOutput().writeBytes(passwordBasedEncryptor.encrypt(linkInfo));
    Boolean isRegistered = response.readBoolean();
    response.close();
    clientTransport.confirmUser(sessionId, confirmation.toByteArray());
    notConnected = false;
    return isRegistered;
  }

  public boolean rename(String newName, char[] newPassword, char[] previousPassword) throws UserAlreadyExists, BadPassword {

    PasswordBasedEncryptor newPasswordBasedEncryptor = new MD5PasswordBasedEncryptor(salt, newPassword, count);
    RedirectPasswordBasedEncryptor encryptor = (RedirectPasswordBasedEncryptor)directory.get(PasswordBasedEncryptor.class);

    PasswordBasedEncryptor confirmPasswordBasedEncryptor = new MD5PasswordBasedEncryptor(salt, previousPassword, count);
    byte[] previousLinkInfo = linkInfo;

    if (!Arrays.equals(confirmPasswordBasedEncryptor.encrypt(SOME_TEXT_TO_CHECK),
                       encryptor.encrypt(SOME_TEXT_TO_CHECK))){
      throw new BadPassword(this.name + " not identified correctly");
    }

    SerializedByteArrayOutput confirmation = new SerializedByteArrayOutput();
    SerializedOutput output = confirmation.getOutput();
    output.writeBytes(privateId);

    MapOfMaps<String, Integer, SerializableGlobType> serverData = getServerData();
    for (SerializableGlobType serializableGlobType : serverData.values()) {
      serializableGlobType.setData(
        newPasswordBasedEncryptor.encrypt(encryptor.decrypt(serializableGlobType.getData())));
    }
    output.writeUtf8String(newName);
    output.writeUtf8String(this.name);
    boolean autoLogin = false;
    output.write(autoLogin);

    output.writeBytes(cryptPassword(newPassword, newPasswordBasedEncryptor));

    output.writeBytes(previousLinkInfo);
    output.writeBytes(confirmPasswordBasedEncryptor.encrypt(previousLinkInfo));

    byte[] linkInfo = generateLinkInfo();
    output.writeBytes(linkInfo);
    output.writeBytes(newPasswordBasedEncryptor.encrypt(linkInfo));

    SerializableGlobSerializer.serialize(output, serverData);

    SerializedInput input = clientTransport.rename(sessionId, confirmation.toByteArray());
    Boolean done = input.readBoolean();
    input.close();
    if (!done){
        return false;
    }
    else{
      encryptor.setPasswordBasedEncryptor(newPasswordBasedEncryptor);
      this.name = newName;
    }
    return true;
  }

  public List<SnapshotInfo> getSnapshotInfos() {
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    SerializedInput input = clientTransport.getSnapshotInfos(sessionId, outputStream.toByteArray());
    int count = input.readNotNullInt();
    List<SnapshotInfo> snapshotInfos = new ArrayList<SnapshotInfo>(count);
    for (int i = 0; i < count; i++){
      // keep order
      long timestamp = input.readNotNullLong();
      long version = input.readNotNullLong();
      String fileName = input.readUtf8String();
      String password = input.readJavaString();
      snapshotInfos.add(new SnapshotInfo(timestamp, fileName, password));
    }
    input.close();
    return snapshotInfos;
  }

  public MapOfMaps<String, Integer, SerializableGlobType> getSnapshotData(SnapshotInfo info, IdUpdater idUpdater) {
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    SerializedOutput output = outputStream.getOutput();
    output.writeBytes(privateId);
    output.writeUtf8String(info.file);
    SerializedInput input = clientTransport.getSnapshotData(sessionId, outputStream.toByteArray());
    MapOfMaps<String, Integer, SerializableGlobType> data = new MapOfMaps<String, Integer, SerializableGlobType>();
    SerializableGlobSerializer.deserialize(input, data);
    input.close();
    return data;
  }

  public void publishDataForMobile(String mail, byte[] data) {

  }

  public void localRegister(byte[] mail, byte[] signature, String activationCode, long jarVersion) {
    clientTransport.localRegister(sessionId, privateId, mail, signature, activationCode);
    clientTransport.localDownload(sessionId, privateId, jarVersion);
  }

  public void downloadedVersion(long version) {
    clientTransport.localDownload(sessionId, privateId, version);
  }

  public List<UserInfo> getLocalUsers() {
    SerializedInput input = clientTransport.getLocalUsers();
    if (input == null){
      return Collections.emptyList();
    }
    int size = input.readNotNullInt();
    List<UserInfo> users = new ArrayList<UserInfo>(size);
    for (int i = 0; i < size; i++) {
      String userName = input.readUtf8String();
      Boolean autoLogin = input.readBoolean();
      users.add(new UserInfo(userName, autoLogin));
    }
    input.close();
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

  private byte[] cryptPassword(char[] name, PasswordBasedEncryptor passwordEncryptor) {
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
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    clientTransport.takeSnapshot(sessionId, outputStream.toByteArray());
  }

  private void updateUserData(byte[] bytes) {
    checkConnected();
    clientTransport.updateUserData(sessionId, bytes).close();
  }

  public MapOfMaps<String, Integer, SerializableGlobType> getServerData() {
    checkConnected();
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    SerializedInput input = clientTransport.getUserData(sessionId, outputStream.toByteArray());
    MapOfMaps<String, Integer, SerializableGlobType> data = new MapOfMaps<String, Integer, SerializableGlobType>();
    SerializableGlobSerializer.deserialize(input, data);
    input.close();
    return data;
  }


  public void replaceData(MapOfMaps<String, Integer, SerializableGlobType> data) {
    checkConnected();
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    SerializableGlobSerializer.serialize(outputStream.getOutput(), data);

    clientTransport.restore(sessionId, outputStream.toByteArray()).close();
  }

  public GlobList getUserData(MutableChangeSet changeSet, IdUpdater idUpdater) {
    checkConnected();
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    SerializedInput input = clientTransport.getUserData(sessionId, outputStream.toByteArray());
    MapOfMaps<String, Integer, SerializableGlobType> data = new MapOfMaps<String, Integer, SerializableGlobType>();
    SerializableGlobSerializer.deserialize(input, data);
    input.close();
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
      else if (globTypeName.equals("bb") || globTypeName.equals("ab")) {  // version 88->91 : ab, 92 : bb  pour csvMapping
        globType = globModel.getType("csvMapping");
      }
      else if (globTypeName.equals("r")){
        globType = globModel.getType("accountPositionError");
      }
      else {
        globType = globModel.getType(globTypeName);
      }
      PicsouGlobSerializer globSerializer =
        globType.getProperty(SerializationManager.SERIALIZATION_PROPERTY, null);
      if (globSerializer == null) {
        if (!SerializationManager.REMOVED_GLOB.contains(globType)){
          throw new RuntimeException("missing serialializer for " + globTypeName);
        }
        continue;
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
    notConnected = true;
  }

}
