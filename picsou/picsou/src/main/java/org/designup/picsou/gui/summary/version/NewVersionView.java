package org.designup.picsou.gui.summary.version;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.browsing.BrowsingAction;
import org.designup.picsou.model.AppVersionInformation;
import org.designup.picsou.model.UserVersionInformation;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class NewVersionView extends View {
  private JPanel newVersionPanel;
  private boolean forceHidden = false;

  public NewVersionView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/summary/newVersionView.splits",
                                                      repository, directory);

    newVersionPanel = new JPanel();
    builder.add("newVersionPanel", newVersionPanel);
    newVersionPanel.setVisible(false);

    builder.add("showChangeLog", new BrowsingAction(Lang.get("newVersion.link.text"), directory) {
      protected String getUrl() {
        return Lang.get("newVersion.link.url");
      }
    });
    builder.add("hide", new HideAction());

    parentBuilder.add("newVersionView", builder);

    repository.addChangeListener(new VersionChangeSetListener());
  }

  private void setMessageVisible(boolean visible) {
    newVersionPanel.setVisible(visible);
    GuiUtils.revalidate(newVersionPanel);
  }

  private class VersionChangeSetListener implements ChangeSetListener {

    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      if (changeSet.containsChanges(AppVersionInformation.KEY) ||
          changeSet.containsChanges(UserVersionInformation.KEY)) {
        updateVersionLabel();
      }
    }
    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      updateVersionLabel();
    }

    private void updateVersionLabel() {
      if (forceHidden) {
        setMessageVisible(false);
        return;
      }

      Glob appVersion = repository.find(AppVersionInformation.KEY);
      Glob userVersion = repository.find(UserVersionInformation.KEY);
      if (appVersion == null || userVersion == null) {
        return;
      }
      Long latestVersion = appVersion.get(AppVersionInformation.LATEST_AVALAIBLE_JAR_VERSION);
      Long currentVersion = userVersion.get(UserVersionInformation.CURRENT_JAR_VERSION);
      if (currentVersion != null && latestVersion != null && currentVersion < latestVersion) {
        setMessageVisible(true);
      }
      else {
        setMessageVisible(false);
      }
    }
  }

  private class HideAction extends AbstractAction {

    private HideAction() {
      super(Lang.get("newVersion.hide.text"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      forceHidden = true;
      setMessageVisible(false);
    }
  }
}
