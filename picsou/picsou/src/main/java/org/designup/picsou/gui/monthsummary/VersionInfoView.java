package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.View;
import org.designup.picsou.model.UserVersionInformation;
import org.designup.picsou.model.AppVersionInformation;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class VersionInfoView extends View {
  private JLabel versionInfo;

  public VersionInfoView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    versionInfo = new JLabel();
    parentBuilder.add("newVersionMessage", versionInfo);
    repository.addChangeListener(new VersionChangeSetListener());
  }

  private class VersionChangeSetListener implements ChangeSetListener {
    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      if (changeSet.containsChanges(AppVersionInformation.KEY) || 
          changeSet.containsChanges(UserVersionInformation.KEY)) {
        updateVersionLabel(repository);
      }
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      updateVersionLabel(repository);
    }

    private void updateVersionLabel(GlobRepository repository) {
      Glob appVersion = repository.find(AppVersionInformation.KEY);
      Glob userVersion = repository.find(UserVersionInformation.KEY);
      if (appVersion == null || userVersion == null) {
        return;
      }
      Long latestVersion = appVersion.get(AppVersionInformation.LATEST_AVALAIBLE_JAR_VERSION);
      Long currentVersion = userVersion.get(UserVersionInformation.CURRENT_JAR_VERSION);
      if (currentVersion != null && latestVersion != null && currentVersion < latestVersion) {
        versionInfo.setText(Lang.get("infoView.new.version"));
        versionInfo.setVisible(true);
      }
      else {
        versionInfo.setVisible(false);
      }
    }
  }
}
