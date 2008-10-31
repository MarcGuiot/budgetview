package org.designup.picsou.gui.license;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.View;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.Millis;

import javax.swing.*;
import java.util.Set;

public class LicenseInfoView extends View {
  private JLabel licenseInfo;

  public LicenseInfoView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.repository = repository;
    this.directory = directory;
    licenseInfo = new JLabel();

    Glob userPreferences = repository.get(UserPreferences.KEY);
    update(userPreferences);
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(UserPreferences.KEY)) {
          Glob userPreferences = repository.get(UserPreferences.KEY);
          update(userPreferences);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        Glob userPreferences = repository.get(UserPreferences.KEY);
        update(userPreferences);
      }
    });
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("licenseMessage", licenseInfo);
  }

  private void update(Glob preferences) {
    if (preferences.get(UserPreferences.REGISTERED_USER)) {
      licenseInfo.setVisible(false);
    }
    else {
      licenseInfo.setVisible(true);
      long days =
        (preferences.get(UserPreferences.LAST_VALID_DAY).getTime() - TimeService.getToday().getTime()) / Millis.ONE_DAY;
      if (days >= 1) {
        licenseInfo.setText(Lang.get("license.info.message", days));
      }
      else if (days == 0) {
        licenseInfo.setText(Lang.get("license.info.last.message"));
      }
      else {
        licenseInfo.setText(Lang.get("license.expiration.message"));
      }
    }
  }
}
