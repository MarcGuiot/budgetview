package org.designup.picsou.gui.startup;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.http.EncrypterToTransportServerAccess;
import org.designup.picsou.client.http.MD5PasswordBasedEncryptor;
import org.designup.picsou.client.http.PasswordBasedEncryptor;
import org.designup.picsou.gui.upgrade.UpgradeTrigger;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.persistence.direct.ReadOnlyAccountDataManager;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
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
    ReadOnlyAccountDataManager.writeSnapshot_V2(serverData, file);
  }

  public boolean restore(InputStream stream, char[] passwd) throws InvalidData {
    MapOfMaps<String, Integer, SerializableGlobType> serverData =
      new MapOfMaps<String, Integer, SerializableGlobType>();
    ReadOnlyAccountDataManager.readSnapshot(serverData, stream);
    PasswordBasedEncryptor passwordBasedEncryptor;
    if (passwd == null) {
      passwordBasedEncryptor = directory.get(PasswordBasedEncryptor.class);
    }
    else {
      passwordBasedEncryptor = new MD5PasswordBasedEncryptor(EncrypterToTransportServerAccess.salt,
                                                             passwd, EncrypterToTransportServerAccess.count);
    }
    GlobModel globModel = directory.get(GlobModel.class);
    try {
      EncrypterToTransportServerAccess.decrypt(new ServerAccess.IdUpdater() {
        public void update(IntegerField field, Integer lastAllocatedId) {
        }
      }, serverData, passwordBasedEncryptor, globModel);
    }
    catch (Exception e) {
      Log.write("decrypt failed : ", e);
      return false;
    }

    if (passwd != null) {
      PasswordBasedEncryptor userPasswordBasedEncryptor = directory.get(PasswordBasedEncryptor.class);

      for (SerializableGlobType serializableGlobType : serverData.values()) {
        serializableGlobType.setData(
          userPasswordBasedEncryptor.encrypt(
            passwordBasedEncryptor.decrypt(serializableGlobType.getData())));
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
      Collection<GlobType> serverTypes = userData.getTypes();
      repository.reset(userData, serverTypes.toArray(new GlobType[serverTypes.size()]));
    }
    finally {
      repository.completeChangeSet();
    }
    repository.removeTrigger(upgradeTrigger);
    return true;
  }
}