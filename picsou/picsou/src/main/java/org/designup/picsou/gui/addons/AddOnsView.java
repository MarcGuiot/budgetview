package org.designup.picsou.gui.addons;

import org.designup.picsou.gui.View;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AddOnsView extends View {

  private JPanel panel = new JPanel();

  public AddOnsView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/addons/addOnsView.splits",
                                                      repository, directory);

    builder.add("addOnsView", panel);
    builder.add("buy", new BuyAddOnAction());

    parentBuilder.add("addOnsView", builder);
  }

  private class BuyAddOnAction extends AbstractAction {
    public BuyAddOnAction() {
      super(Lang.get("addons.buy"));
    }

    public void actionPerformed(ActionEvent e) {
    }
  }
}
