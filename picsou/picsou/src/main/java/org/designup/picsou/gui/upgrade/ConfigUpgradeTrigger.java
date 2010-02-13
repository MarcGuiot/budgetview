package org.designup.picsou.gui.upgrade;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.AppVersionInformation;
import org.designup.picsou.model.UserVersionInformation;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class ConfigUpgradeTrigger implements ChangeSetListener {
  private Directory directory;

  public ConfigUpgradeTrigger(Directory directory) {
    this.directory = directory;
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(UserVersionInformation.KEY) ||
        changeSet.containsChanges(AppVersionInformation.KEY)) {
      Glob appVersion = repository.find(AppVersionInformation.KEY);
      Glob userVersion = repository.find(UserVersionInformation.KEY);
      if (appVersion == null || userVersion == null) {
        return;
      }
      if (userVersion.get(UserVersionInformation.CURRENT_BANK_CONFIG_VERSION)
          < (appVersion.get(AppVersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION))) {
        directory.get(UpgradeService.class).upgradeBankData(repository, appVersion);
      }
    }
    changeSet.safeVisit(Account.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Account.BANK)) {
          directory.get(UpgradeService.class).updateOperations(repository, repository.get(key));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
