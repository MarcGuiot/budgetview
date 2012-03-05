package org.designup.picsou.gui.summary.version;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.browsing.BrowsingAction;
import org.designup.picsou.gui.components.FooterBanner;
import org.designup.picsou.model.AppVersionInformation;
import org.designup.picsou.model.UserVersionInformation;
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

public class NewVersionView extends View {
  private JPanel newVersionPanel;
  private boolean forceHidden = false;
  private FooterBanner banner;

  public NewVersionView(GlobRepository repository, Directory directory) {
    super(repository, directory);

    BrowsingAction action = new BrowsingAction(Lang.get("newVersion.link.text"), directory) {
      protected String getUrl() {
        return Lang.get("newVersion.link.url");
      }
    };
    banner = new FooterBanner(Lang.get("newVersion.message"), action, true, repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    parentBuilder.add("newVersionView", banner.getPanel());

    repository.addChangeListener(new VersionChangeSetListener());
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
        banner.setVisible(false);
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
        banner.setVisible(true);
      }
      else {
        banner.setVisible(false);
      }
    }
  }
}
