package org.designup.picsou.gui.general;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.browsing.BrowsingAction;
import org.designup.picsou.gui.help.actions.GotoWebsiteAction;
import org.designup.picsou.gui.license.AddOnStatusVisibilityUpdater;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class AddonsView extends View {

  private JPanel panel;

  public AddonsView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/general/addonsView.splits",
                                                      repository, directory);

    panel = new JPanel();
    builder.add("addonsView", panel);

    AddOnStatusVisibilityUpdater.installReversed(repository, panel);

    builder.add("gotoWebsite", new BrowsingAction(directory) {
      protected String getUrl() {
        return Lang.get("addons.url");
      }
    });

    parentBuilder.add("addonsView", builder.load());

  }
}
