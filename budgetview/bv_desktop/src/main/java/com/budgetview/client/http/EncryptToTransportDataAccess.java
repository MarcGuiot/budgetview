package com.budgetview.client.http;

import com.budgetview.client.serialization.ChangeSetSerializerVisitor;
import com.budgetview.session.serialization.SerializedGlob;
import com.budgetview.shared.encryption.PasswordBasedEncryptor;
import com.budgetview.shared.encryption.RedirectPasswordBasedEncryptor;
import com.budgetview.shared.encryption.MD5PasswordBasedEncryptor;
import com.budgetview.shared.utils.GlobSerializer;
import com.budgetview.client.DataTransport;
import com.budgetview.client.serialization.SerializableDeltaGlobSerializer;
import com.budgetview.client.serialization.GlobCollectionSerializer;
import com.budgetview.client.DataAccess;
import com.budgetview.client.exceptions.BadConnection;
import com.budgetview.client.exceptions.BadPassword;
import com.budgetview.client.exceptions.UserAlreadyExists;
import com.budgetview.session.serialization.SerializedDelta;
import com.budgetview.session.serialization.SerializationManager;
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

public class EncryptToTransportDataAccess implements DataAccess {
  private DataTransport dataTransport;
  private Directory directory;
  private String name;
  private Long sessionId;
  private byte[] privateId;
  private boolean notConnected = true;
  public static final byte[] salt = {0x54, 0x12, 0x43, 0x65, 0x77, 0x2, 0x79, 0x72};
  private GlobModel globModel;
  public static int count = 20;
  private static final byte[] SOME_TEXT_TO_CHECK = "some text to check".getBytes();
  private byte[] linkInfo;

  public EncryptToTransportDataAccess(DataTransport transport, Directory directory) {
    this.dataTransport = transport;
    this.directory = directory;
    globModel = directory.get(GlobModel.class);
  }

  public LocalInfo connect(long version) {
    SerializedInput response = dataTransport.connect(version);
    try {
      if (response.readBoolean()) {
        byte[] repoId = response.readBytes();
        byte[] mail = response.readBytes();
        byte[] signature = response.readBytes();
        String activationCode = response.readJavaString();
        long count = response.readNotNullLong();
        long downloadVersion = response.readNotNullLong();
        String lang = response.readUtf8String();
        long jarVersion = response.readNotNullLong();
        sessionId = response.readLong();
        privateId = response.readBytes();
        return new LocalInfo(repoId, mail, signature, activationCode, count, downloadVersion, lang, jarVersion);
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
      RedirectPasswordBasedEncryptor encryptor = (RedirectPasswordBasedEncryptor) directory.get(PasswordBasedEncryptor.class);
      encryptor.setPasswordBasedEncryptor(passwordBasedEncryptor);

      SerializedByteArrayOutput request = new SerializedByteArrayOutput();
      SerializedOutput output = request.getOutput();
      output.writeUtf8String(this.name);
      output.write(autoLogin);
      output.writeBytes(cryptPassword(password, passwordBasedEncryptor));

      linkInfo = generateLinkInfo();
      output.writeBytes(linkInfo);
      output.writeBytes(passwordBasedEncryptor.encrypt(linkInfo));

      response = dataTransport.createUser(sessionId, request.toByteArray());
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
      if (response != null) {
        response.close();
      }
    }
  }

  public void deleteUser(String name, char[] password) {
    PasswordBasedEncryptor passwordBasedEncryptor = new MD5PasswordBasedEncryptor(salt, password, count);
    RedirectPasswordBasedEncryptor encryptor = (RedirectPasswordBasedEncryptor) directory.get(PasswordBasedEncryptor.class);
    encryptor.setPasswordBasedEncryptor(passwordBasedEncryptor);

    linkInfo = requestLinkInfo(name, password, passwordBasedEncryptor);

    SerializedByteArrayOutput confirmation = new SerializedByteArrayOutput();
    confirmation.getOutput().writeBytes(privateId);
    confirmation.getOutput().writeBytes(passwordBasedEncryptor.encrypt(linkInfo));
    dataTransport.deleteUser(sessionId, confirmation.toByteArray()).close();
  }

  private byte[] requestLinkInfo(String name, char[] password, PasswordBasedEncryptor passwordBasedEncryptor) {
    SerializedByteArrayOutput request = new SerializedByteArrayOutput();
    SerializedOutput output = request.getOutput();
    output.writeUtf8String(name);
    output.writeBytes(cryptPassword(password, passwordBasedEncryptor));

    SerializedInput response = dataTransport.identifyUser(sessionId, request.toByteArray());
    byte[] bytes = response.readBytes();
    response.close();
    return bytes;
  }

  public boolean initConnection(String name, char[] password, boolean privateComputer) {
    this.name = name;

    PasswordBasedEncryptor passwordBasedEncryptor = new MD5PasswordBasedEncryptor(salt, password, count);
    RedirectPasswordBasedEncryptor encryptor = (RedirectPasswordBasedEncryptor) directory.get(PasswordBasedEncryptor.class);
    encryptor.setPasswordBasedEncryptor(passwordBasedEncryptor);

    SerializedByteArrayOutput request = new SerializedByteArrayOutput();
    SerializedOutput output = request.getOutput();

    output.writeUtf8String(this.name);
    output.writeBytes(cryptPassword(password, passwordBasedEncryptor));

    SerializedInput response = dataTransport.identifyUser(sessionId, request.toByteArray());

    SerializedByteArrayOutput confirmation = new SerializedByteArrayOutput();
    confirmation.getOutput().writeBytes(privateId);
    linkInfo = response.readBytes();
    confirmation.getOutput().writeBytes(passwordBasedEncryptor.encrypt(linkInfo));
    Boolean isRegistered = response.readBoolean();
    response.close();
    dataTransport.confirmUser(sessionId, confirmation.toByteArray());
    notConnected = false;
    return isRegistered;
  }

  public boolean rename(String newName, char[] newPassword, char[] previousPassword) throws UserAlreadyExists, BadPassword {

    PasswordBasedEncryptor newPasswordBasedEncryptor = new MD5PasswordBasedEncryptor(salt, newPassword, count);
    RedirectPasswordBasedEncryptor encryptor = (RedirectPasswordBasedEncryptor) directory.get(PasswordBasedEncryptor.class);

    PasswordBasedEncryptor confirmPasswordBasedEncryptor = new MD5PasswordBasedEncryptor(salt, previousPassword, count);
    byte[] previousLinkInfo = linkInfo;

    if (!Arrays.equals(confirmPasswordBasedEncryptor.encrypt(SOME_TEXT_TO_CHECK),
                       encryptor.encrypt(SOME_TEXT_TO_CHECK))) {
      throw new BadPassword(this.name + " not identified correctly");
    }

    SerializedByteArrayOutput confirmation = new SerializedByteArrayOutput();
    SerializedOutput output = confirmation.getOutput();
    output.writeBytes(privateId);

    MapOfMaps<String, Integer, SerializedGlob> serverData = getServerData();
    for (SerializedGlob serializedGlob : serverData.values()) {
      serializedGlob.setData(
        newPasswordBasedEncryptor.encrypt(encryptor.decrypt(serializedGlob.getData())));
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

    GlobCollectionSerializer.serialize(output, serverData);

    SerializedInput input = dataTransport.rename(sessionId, confirmation.toByteArray());
    Boolean done = input.readBoolean();
    input.close();
    if (!done) {
      return false;
    }
    else {
      encryptor.setPasswordBasedEncryptor(newPasswordBasedEncryptor);
      this.name = newName;
    }
    return true;
  }

  public List<SnapshotInfo> getSnapshotInfos() {
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    SerializedInput input = dataTransport.getSnapshotInfos(sessionId, outputStream.toByteArray());
    int count = input.readNotNullInt();
    List<SnapshotInfo> snapshotInfos = new ArrayList<SnapshotInfo>(count);
    for (int i = 0; i < count; i++) {
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

  public MapOfMaps<String, Integer, SerializedGlob> getSnapshotData(SnapshotInfo info, IdUpdater idUpdater) {
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    SerializedOutput output = outputStream.getOutput();
    output.writeBytes(privateId);
    output.writeUtf8String(info.file);
    SerializedInput input = dataTransport.getSnapshotData(sessionId, outputStream.toByteArray());
    MapOfMaps<String, Integer, SerializedGlob> data = new MapOfMaps<String, Integer, SerializedGlob>();
    GlobCollectionSerializer.deserialize(input, data);
    input.close();
    return data;
  }

  public void localRegister(byte[] mail, byte[] signature, String activationCode, long jarVersion) {
    dataTransport.localRegister(sessionId, privateId, mail, signature, activationCode);
    dataTransport.localDownload(sessionId, privateId, jarVersion);
  }

  public void downloadedVersion(long version) {
    dataTransport.localDownload(sessionId, privateId, version);
  }

  public void setLang(String lang) {
    dataTransport.setLang(sessionId, privateId, lang);
  }

  public List<UserInfo> getLocalUsers() {
    SerializedInput input = dataTransport.getLocalUsers();
    if (input == null) {
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
    dataTransport.removeLocalUser(user);
  }

  public boolean canRead(MapOfMaps<String, Integer, SerializedGlob> data) {
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
        tab[i] = (byte) c;
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
    MultiMap<String, SerializedDelta> stringDeltaGlobMultiMap = serializerVisitor.getSerializableGlob();
    SerializableDeltaGlobSerializer.serialize(outputStream.getOutput(), stringDeltaGlobMultiMap);
    if (stringDeltaGlobMultiMap.size() != 0) {
      updateUserData(outputStream.toByteArray());
    }
  }

  public boolean hasChanged() {
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    SerializedInput serializedInput = dataTransport.hasChanged(sessionId, outputStream.toByteArray());
    return serializedInput.readBoolean();
  }

  public void takeSnapshot() {
    if (notConnected) {
      return;
    }
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    dataTransport.takeSnapshot(sessionId, outputStream.toByteArray());
  }

  private void updateUserData(byte[] bytes) {
    checkConnected();
    dataTransport.updateUserData(sessionId, bytes).close();
  }

  public MapOfMaps<String, Integer, SerializedGlob> getServerData() {
    checkConnected();
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    SerializedInput input = dataTransport.getUserData(sessionId, outputStream.toByteArray());
    MapOfMaps<String, Integer, SerializedGlob> data = new MapOfMaps<String, Integer, SerializedGlob>();
    GlobCollectionSerializer.deserialize(input, data);
    input.close();
    return data;
  }


  public void replaceData(MapOfMaps<String, Integer, SerializedGlob> data) {
    checkConnected();
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    GlobCollectionSerializer.serialize(outputStream.getOutput(), data);

    dataTransport.restore(sessionId, outputStream.toByteArray()).close();
  }

  public GlobList getUserData(MutableChangeSet changeSet, IdUpdater idUpdater) {
    checkConnected();
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    outputStream.getOutput().writeBytes(privateId);
    SerializedInput input = dataTransport.getUserData(sessionId, outputStream.toByteArray());
    MapOfMaps<String, Integer, SerializedGlob> data = new MapOfMaps<String, Integer, SerializedGlob>();
    GlobCollectionSerializer.deserialize(input, data);
    input.close();
    return decrypt(idUpdater, data, directory.get(PasswordBasedEncryptor.class), globModel);
  }

  public static GlobList decrypt(IdUpdater idUpdater, MapOfMaps<String, Integer, SerializedGlob> data,
                                 final PasswordBasedEncryptor passwordBasedEncryptor, final GlobModel globModel) {
    GlobList result = new GlobList(data.size());

    for (String globTypeName : data.keys()) {
      GlobType globType = getGlobType(globModel, globTypeName);
      GlobSerializer globSerializer =
        globType.getProperty(SerializationManager.SERIALIZATION_PROPERTY, null);
      if (globSerializer == null) {
        if (!SerializationManager.REMOVED_GLOB.contains(globType)) {
          throw new RuntimeException("missing serialializer for " + globTypeName);
        }
        continue;
      }
      IntegerField field = (IntegerField) globType.getKeyFields()[0];
      Integer id;
      Integer maxId = 0;
      for (Map.Entry<Integer, SerializedGlob> globEntry : data.get(globTypeName).entrySet()) {
        id = globEntry.getKey();
        GlobBuilder globBuilder = GlobBuilder.init(globType).setValue(field, id);
        SerializedGlob globData = globEntry.getValue();
        try {
          globSerializer.deserializeData(globData.getVersion(),
                                         passwordBasedEncryptor.decrypt(globData.getData()), id, globBuilder
          );
        }
        catch (Exception e) {
          throw new RuntimeException("Error loading type " + globType + " with version " + globData.getVersion(), e);
        }
        result.add(globBuilder.get());
        maxId = Math.max(maxId, id);
      }
      idUpdater.update(field, maxId);
    }
    return result;
  }

  public static GlobType getGlobType(GlobModel globModel, String globTypeName) {
    GlobType globType;
    if (globTypeName.equals("currenMonth")) {
      globType = globModel.getType("currentMonth");
    }
    else if (globTypeName.equals("bb") || globTypeName.equals("ab")) {  // version 88->91 : ab, 92 : bb  pour csvMapping
      globType = globModel.getType("csvMapping");
    }
    else if (globTypeName.equals("r")) {
      globType = globModel.getType("accountPositionError");
    }
    else {
      globType = globModel.getType(globTypeName);
    }
    return globType;
  }

  public void disconnect() {
    if (notConnected) {
      return;
    }
    SerializedByteArrayOutput request = new SerializedByteArrayOutput();
    request.getOutput().writeBytes(privateId);
    dataTransport.disconnect(sessionId, request.toByteArray());
    notConnected = true;
  }

}
