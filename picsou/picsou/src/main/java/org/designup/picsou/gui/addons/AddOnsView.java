package org.designup.picsou.gui.addons;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.startup.components.LogoutService;
import org.designup.picsou.model.AddOns;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.splits.utils.OnLoadListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AddOnsView extends View {

  private JPanel panel = new JPanel();
  private AddOnService addOnService;

  public AddOnsView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.addOnService = directory.get(AddOnService.class);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/addons/addOnsView.splits",
                                                      repository, directory);

    builder.add("addonsView", panel);

    builder.addRepeat("addOnSelectors", addOnService.getAddOns(), new AddOnSelectorFactory());

    parentBuilder.add("addonsView", builder);
  }

  private class AddOnSelectorFactory implements RepeatComponentFactory<AddOn> {

    public void registerComponents(PanelBuilder cellBuilder, final AddOn addOn) {

      cellBuilder.add("label", new JLabel(addOn.getName()));
      cellBuilder.add("image", new JLabel(addOn.getIcon()));

      final CardHandler cards = cellBuilder.addCardHandler("cards");

      final KeyChangeListener updater = new KeyChangeListener(AddOns.KEY) {
        public void update() {
          cards.show(addOn.isEnabled(repository) ? "enabled" : "disabled");
        }
      };
      repository.addChangeListener(updater);
      cellBuilder.addDisposable(new Disposable() {
        public void dispose() {
          repository.removeChangeListener(updater);
        }
      });

      cellBuilder.add("buy", new BuyAddOnAction(addOn));
      cellBuilder.add("gotoDemoAccount", new ShowInDemoAccountAction(directory));

      cellBuilder.add("description", GuiUtils.createReadOnlyHtmlComponent(addOn.getDescription()));

      cellBuilder.addOnLoadListener(new OnLoadListener() {
        public void processLoad() {
          updater.update();
        }
      });
    }
  }

  private class BuyAddOnAction extends AbstractAction {
    private AddOn addOn;

    public BuyAddOnAction(AddOn addOn) {
      super(Lang.get("addons.learnMore"));
      this.addOn = addOn;
    }

    public void actionPerformed(ActionEvent e) {
      addOn.activate(repository, directory);
    }
  }

  public class ShowInDemoAccountAction extends AbstractAction {

    private Directory directory;

    public ShowInDemoAccountAction(Directory directory) {
      super(Lang.get("addons.example"));
      this.directory = directory;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      directory.get(LogoutService.class).gotoDemoAccount();
    }
  }
}
