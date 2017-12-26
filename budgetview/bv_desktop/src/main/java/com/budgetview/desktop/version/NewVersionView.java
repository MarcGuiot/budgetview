package com.budgetview.desktop.version;

import com.budgetview.desktop.Application;
import com.budgetview.desktop.View;
import com.budgetview.desktop.browsing.BrowsingAction;
import com.budgetview.desktop.components.FooterBanner;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class NewVersionView extends View {
  private FooterBanner footer;

  public NewVersionView(GlobRepository repository, Directory directory) {
    super(repository, directory);

    BrowsingAction action = new BrowsingAction(Lang.get("newVersion.link.text"), directory) {
      protected String getUrl() {
        return Lang.get("newVersion.link.url");
      }
    };
    footer = new FooterBanner("", action, true, repository, directory);
    footer.setVisible(false);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    parentBuilder.add("newVersionView", footer.getPanel());
  }

  public void reset() {
    directory.get(NewVersionService.class).update(new NewVersionService.Listener() {
      public void update(boolean newVersionAvailable, String currentVersion, String newVersion) {
        if (newVersionAvailable) {
          footer.show(Lang.get("newVersion.message", currentVersion, newVersion));
        }
        else {
          footer.hide();
        }
      }
    });
  }
}
