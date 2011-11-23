package org.designup.picsou.gui.backup;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.http.EncrypterToTransportServerAccess;
import org.designup.picsou.client.http.MD5PasswordBasedEncryptor;
import org.designup.picsou.client.http.PasswordBasedEncryptor;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.upgrade.UpgradeTrigger;
import org.designup.picsou.model.User;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.persistence.direct.ReadOnlyAccountDataManager;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.repository.DefaultGlobIdGenerator;
import org.globsframework.utils.Files;
import org.globsframework.utils.collections.MapOfMaps;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public class BackupService {
  private ServerAccess serverAccess;
  private Directory directory;
  private GlobRepository repository;
  private DefaultGlobIdGenerator idGenerator;
  private UpgradeTrigger upgradeTrigger;

  public enum Status {
    BAD_VERSION,
    OK,
    DECRYPT_FAILED
  }

  public BackupService(ServerAccess serverAccess,
                       Directory directory, GlobRepository repository,
                       DefaultGlobIdGenerator idGenerator,
                       UpgradeTrigger upgradeTrigger) {
    this.serverAccess = serverAccess;
    this.directory = directory;
    this.repository = repository;
    this.idGenerator = idGenerator;
    this.upgradeTrigger = upgradeTrigger;
  }

  public void generate(File file) throws IOException {
    MapOfMaps<String, Integer, SerializableGlobType> serverData = serverAccess.getServerData();
    Files.createParentDirs(file);
    Glob user = repository.find(User.KEY);
    char[] password = null;
    if (user.isTrue(User.AUTO_LOGIN)) {
      password = user.get(User.NAME).toCharArray();
    }
    long timestamp = System.currentTimeMillis();
    ReadOnlyAccountDataManager.writeSnapshot(serverData, file, password, PicsouApplication.JAR_VERSION, timestamp);
  }

  public Status restore(InputStream stream, char[] password) throws InvalidData {
    MapOfMaps<String, Integer, SerializableGlobType> serverData =
      new MapOfMaps<String, Integer, SerializableGlobType>();
    ReadOnlyAccountDataManager.SnapshotInfo snapshotInfo = ReadOnlyAccountDataManager.readSnapshot(serverData, stream);
    if (snapshotInfo.version > PicsouApplication.JAR_VERSION) {
      return Status.BAD_VERSION;
    }
    return restore(password, serverData, snapshotInfo.password);
  }

  public Status restore(char[] password, MapOfMaps<String, Integer, SerializableGlobType> serverData, final char[] autoLogPassword) {
    PasswordBasedEncryptor readPasswordBasedEncryptor;
    PasswordBasedEncryptor writeBasedEncryptor = directory.get(PasswordBasedEncryptor.class);
    if (autoLogPassword != null) {
      readPasswordBasedEncryptor = new MD5PasswordBasedEncryptor(EncrypterToTransportServerAccess.salt,
                                                                 autoLogPassword, EncrypterToTransportServerAccess.count);
    }
    else if (password == null) {
      readPasswordBasedEncryptor = writeBasedEncryptor;
    }
    else {
      readPasswordBasedEncryptor = new MD5PasswordBasedEncryptor(EncrypterToTransportServerAccess.salt,
                                                                 password, EncrypterToTransportServerAccess.count);
    }
    GlobModel globModel = directory.get(GlobModel.class);
    try {
      EncrypterToTransportServerAccess.decrypt(new ServerAccess.IdUpdater() {
        public void update(IntegerField field, Integer lastAllocatedId) {
        }
      }, serverData, readPasswordBasedEncryptor, globModel);
    }
    catch (Exception e) {
      return Status.DECRYPT_FAILED;
    }

    if (readPasswordBasedEncryptor != writeBasedEncryptor) {

      for (SerializableGlobType serializableGlobType : serverData.values()) {
        serializableGlobType.setData(
          writeBasedEncryptor.encrypt(
            readPasswordBasedEncryptor.decrypt(serializableGlobType.getData())));
      }
    }

    serverAccess.replaceData(serverData);

    MutableChangeSet changeSet = new DefaultChangeSet();
    GlobList userData = serverAccess.getUserData(changeSet, new BackupIdUpdater());

    try {
      repository.startChangeSet();
      Collection<GlobType> globTypeCollection = PicsouGuiModel.getUserSpecificType();
      repository.reset(GlobList.EMPTY, globTypeCollection.toArray(new GlobType[globTypeCollection.size()]));
    } catch (Exception e){
      Log.write("Erreur while clearing data (ignored)", e);
    } finally {
      repository.completeChangeSet();
    }

    try {
      repository.startChangeSet();
      repository.addTriggerAtFirst(upgradeTrigger);

      Collection<GlobType> globTypeCollection = PicsouGuiModel.getUserSpecificType();
      repository.reset(userData, globTypeCollection.toArray(new GlobType[globTypeCollection.size()]));
    }
    finally {
      repository.completeChangeSet();
    }
    repository.removeTrigger(upgradeTrigger);
    repository.startChangeSet();
    try {
      upgradeTrigger.postTraitement(repository);
      SignpostStatus.setAllCompleted(repository);
    }
    finally {
      repository.completeChangeSet();
    }
    return Status.OK;
  }

  public boolean rename(String newName, char[] passwd, final char[] previousPasswd){
    return serverAccess.rename(newName, passwd, previousPasswd);
  }

  public List<ServerAccess.SnapshotInfo> getSnapshotInfos(){
    return serverAccess.getSnapshotInfos();
  }

  public MapOfMaps<String, Integer, SerializableGlobType> restore(ServerAccess.SnapshotInfo snapshotInfo){
    return serverAccess.getSnapshotData(snapshotInfo, new BackupIdUpdater());
  }

  private class BackupIdUpdater implements ServerAccess.IdUpdater {
    public void update(IntegerField field, Integer lastAllocatedId) {
      idGenerator.update(field, lastAllocatedId);
    }
  }
}