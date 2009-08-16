package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.VersionInformation;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class VersionInfoView extends View {
  private JEditorPane versionInfo;

  public VersionInfoView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    versionInfo = Gui.createHtmlEditor("");
    parentBuilder.add("newVersionMessage", versionInfo);
    Glob version = repository.get(VersionInformation.KEY);
    Long currentVersion = version.get(VersionInformation.CURRENT_JAR_VERSION);
    Long latestVersion = version.get(VersionInformation.LATEST_AVALAIBLE_JAR_VERSION);
    if (currentVersion != null && latestVersion != null && currentVersion < latestVersion) {
      versionInfo.setText(Lang.get("infoView.new.version"));
      versionInfo.setVisible(true);
    }
    else {
      versionInfo.setVisible(false);
    }
    repository.addChangeListener(new VersionChangeSetListener());
  }

  private class VersionChangeSetListener implements ChangeSetListener {
    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      if (changeSet.containsChanges(VersionInformation.KEY)) {
        FieldValues value = changeSet.getPreviousValue(VersionInformation.KEY);
        if (value.contains(VersionInformation.LATEST_AVALAIBLE_JAR_VERSION)) {
          if (!value.get(VersionInformation.LATEST_AVALAIBLE_JAR_VERSION).equals(
            repository.get(VersionInformation.KEY).get(VersionInformation.CURRENT_JAR_VERSION))) {
            versionInfo.setText(Lang.get("infoView.new.version"));
            versionInfo.setVisible(true);
          }
        }
      }
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    }
  }
}
