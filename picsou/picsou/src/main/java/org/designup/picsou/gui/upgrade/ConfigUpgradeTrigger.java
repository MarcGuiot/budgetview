package org.designup.picsou.gui.upgrade;

import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;
import org.globsframework.metamodel.GlobType;
import org.globsframework.utils.directory.Directory;
import org.designup.picsou.model.UserVersionInformation;
import org.designup.picsou.model.AppVersionInformation;

import java.util.Set;

public class ConfigUpgradeTrigger implements ChangeSetListener{
  private Directory directory;

  public ConfigUpgradeTrigger(Directory directory){
    this.directory = directory;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(UserVersionInformation.KEY) ||
        changeSet.containsChanges(AppVersionInformation.KEY)){
      Glob appVersion = repository.find(AppVersionInformation.KEY);
      Glob userVersion = repository.find(UserVersionInformation.KEY);
      if (appVersion == null || userVersion == null){
        return;
      }
      if (userVersion.get(UserVersionInformation.CURRENT_BANK_CONFIG_VERSION)
          < (appVersion.get(AppVersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION))) {
        directory.get(UpgradeService.class).upgradeBankData(repository, appVersion);
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
