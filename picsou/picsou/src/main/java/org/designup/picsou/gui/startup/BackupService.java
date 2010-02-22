package org.designup.picsou.gui.startup;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.http.EncrypterToTransportServerAccess;
import org.designup.picsou.client.http.MD5PasswordBasedEncryptor;
import org.designup.picsou.client.http.PasswordBasedEncryptor;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.upgrade.UpgradeTrigger;
import org.designup.picsou.model.User;
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
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.utils.Files;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

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
    PasswordBasedEncryptor readPasswordBasedEncryptor;
    PasswordBasedEncryptor writeBasedEncryptor = directory.get(PasswordBasedEncryptor.class);
    if (snapshotInfo.password != null) {
      readPasswordBasedEncryptor = new MD5PasswordBasedEncryptor(EncrypterToTransportServerAccess.salt,
                                                                 snapshotInfo.password, EncrypterToTransportServerAccess.count);
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
    GlobList userData = serverAccess.getUserData(changeSet, new ServerAccess.IdUpdater() {
      public void update(IntegerField field, Integer lastAllocatedId) {
        idGenerator.update(field, lastAllocatedId);
      }
    });

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
    return Status.OK;
  }
}