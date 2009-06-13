package org.designup.picsou.gui.startup;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.upgrade.UpgradeTrigger;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.persistence.direct.ReadOnlyAccountDataManager;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.utils.Files;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.exceptions.InvalidData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class BackupService {
  private ServerAccess serverAccess;
  private GlobRepository repository;
  private DefaultGlobIdGenerator idGenerator;
  private UpgradeTrigger upgradeTrigger;

  public BackupService(ServerAccess serverAccess,
                       GlobRepository repository,
                       DefaultGlobIdGenerator idGenerator,
                       UpgradeTrigger upgradeTrigger) {
    this.serverAccess = serverAccess;
    this.repository = repository;
    this.idGenerator = idGenerator;
    this.upgradeTrigger = upgradeTrigger;
  }

  public void generate(File file) throws IOException {
    MapOfMaps<String, Integer, SerializableGlobType> serverData = serverAccess.getServerData();
    Files.createParentDirs(file);
    ReadOnlyAccountDataManager.writeSnapshot_V2(serverData, file);
  }

  public void restore(InputStream stream) throws InvalidData {
    MapOfMaps<String, Integer, SerializableGlobType> serverData =
      new MapOfMaps<String, Integer, SerializableGlobType>();
    ReadOnlyAccountDataManager.readSnapshot(serverData, stream);
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
  }
}