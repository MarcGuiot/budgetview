package com.budgetview.gui.backup;

import com.budgetview.client.http.EncrypterToTransportServerAccess;
import com.budgetview.http.MD5PasswordBasedEncryptor;
import com.budgetview.client.serialization.PasswordBasedEncryptor;
import com.budgetview.gui.PicsouApplication;
import com.budgetview.gui.PicsouInit;
import com.budgetview.gui.card.NavigationService;
import com.budgetview.model.SignpostStatus;
import com.budgetview.model.User;
import com.budgetview.server.model.SerializableGlobType;
import com.budgetview.triggers.AddOnTrigger;
import com.budgetview.client.ServerAccess;
import com.budgetview.gui.model.PicsouGuiModel;
import com.budgetview.gui.time.TimeService;
import com.budgetview.gui.upgrade.UpgradeTrigger;
import com.budgetview.model.CurrentMonth;
import com.budgetview.server.persistence.direct.ReadOnlyAccountDataManager;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.repository.DefaultGlobIdGenerator;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.globsframework.utils.collections.MapOfMaps;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;

import java.io.*;
import java.util.List;

import static org.globsframework.model.FieldValue.value;

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
      e.printStackTrace();
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
    userData.addAll(PicsouInit.additionalGlobToAdd(repository));

    try {
      repository.startChangeSet();
      repository.reset(GlobList.EMPTY, PicsouGuiModel.getUserSpecificTypes());
    }
    catch (Exception e) {
      Log.write("Error while clearing data (ignored)", e);
    }
    finally {
      repository.completeChangeSet();
    }

    directory.get(NavigationService.class).gotoHomeAfterRestore(userData);

    try {
      repository.startChangeSet();
      repository.addTriggerAtFirst(upgradeTrigger);
      repository.reset(userData, PicsouGuiModel.getUserSpecificTypes());
      AddOnTrigger.alignWithUser(repository);
    }
    finally {
      repository.completeChangeSet();
    }
    repository.removeTrigger(upgradeTrigger);

    repository.startChangeSet();
    try {
      upgradeTrigger.postProcessing(repository);
      SignpostStatus.setAllCompleted(repository);
    }
    finally {
      repository.completeChangeSet();
    }
    repository.update(CurrentMonth.KEY,
                      value(CurrentMonth.CURRENT_MONTH, TimeService.getCurrentMonth()),
                      value(CurrentMonth.CURRENT_DAY, TimeService.getCurrentDay()));
    return Status.OK;
  }

  public boolean rename(String newName, char[] passwd, final char[] previousPasswd) {
    return serverAccess.rename(newName, passwd, previousPasswd);
  }

  public List<ServerAccess.SnapshotInfo> getSnapshotInfos() {
    return serverAccess.getSnapshotInfos();
  }

  public MapOfMaps<String, Integer, SerializableGlobType> restore(ServerAccess.SnapshotInfo snapshotInfo) {
    return serverAccess.getSnapshotData(snapshotInfo, new BackupIdUpdater());
  }

  private class BackupIdUpdater implements ServerAccess.IdUpdater {
    public void update(IntegerField field, Integer lastAllocatedId) {
      idGenerator.update(field, lastAllocatedId);
    }
  }
}