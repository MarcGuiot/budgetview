package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.View;
import org.designup.picsou.model.VersionInformation;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class InfoView extends View {
  private JEditorPane versionInfo;
  private JPanel informationPanel;

  public InfoView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/infoView.splits",
                                                      repository, directory);

    informationPanel = new JPanel();
    builder.add("informationPanel", informationPanel);
    versionInfo = new JEditorPane();
    versionInfo.setContentType("text/html");
    builder.add("versionInfo", versionInfo);
    Glob version = repository.get(VersionInformation.KEY);
    if (!version.get(VersionInformation.CURRENT_JAR_VERSION).equals(
      version.get(VersionInformation.LATEST_AVALAIBLE_JAR_VERSION))) {
      versionInfo.setText(Lang.get("infoView.new.version"));
      informationPanel.setVisible(true);
    }
    else {
      informationPanel.setVisible(false);
    }
    repository.addChangeListener(new VersionChangeSetListener());
    parentBuilder.add("infoView", builder);
  }

  private class VersionChangeSetListener implements ChangeSetListener {
    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      if (changeSet.containsChanges(VersionInformation.KEY)) {
        FieldValues value = changeSet.getPreviousValue(VersionInformation.KEY);
        if (value.contains(VersionInformation.LATEST_AVALAIBLE_JAR_VERSION)) {
          if (!value.get(VersionInformation.LATEST_AVALAIBLE_JAR_VERSION).equals(
            repository.get(VersionInformation.KEY).get(VersionInformation.CURRENT_JAR_VERSION))) {
            versionInfo.setText(Lang.get("infoView.new.version"));
            informationPanel.setVisible(true);
          }
        }
      }
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    }
  }
}
