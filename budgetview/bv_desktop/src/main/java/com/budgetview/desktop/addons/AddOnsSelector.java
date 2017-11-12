package com.budgetview.desktop.addons;

import com.budgetview.desktop.View;
import com.budgetview.desktop.help.actions.GotoWebsiteAction;
import com.budgetview.desktop.license.activation.ActivateLicenseAction;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class AddOnsSelector extends View {

  private JPanel panel = new JPanel();
  private AddOnService addOnService;

  public AddOnsSelector(GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.addOnService = directory.get(AddOnService.class);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/addons/addOnsSelector.splits",
                                                      repository, directory);

    builder.add("buy", new GotoWebsiteAction(Lang.get("addonsSelector.buy.button"), directory) {
      protected String getUrl() {
        return Lang.get("site.buy.addons.url");
      }
    });

    builder.add("activate", new ActivateLicenseAction(Lang.get("addonsSelector.activate.button"), repository, directory));

    parentBuilder.add("addonsSelector", builder);
  }
}
