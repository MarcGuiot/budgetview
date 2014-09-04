package org.designup.picsou.gui.addons;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.startup.components.LogoutService;
import org.designup.picsou.model.AddOns;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
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
import java.awt.event.ActionListener;

public class AddOnsView extends View {

  private JPanel panel = new JPanel();
  private AddOnService addOnService;
  private AddOn selectedAddOn;
  private ButtonGroup group = new ButtonGroup();
  private JEditorPane editor;
  private JLabel imageLabel = new JLabel();
  private CardHandler cards;

  public AddOnsView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.addOnService = directory.get(AddOnService.class);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/addons/addOnsView.splits",
                                                      repository, directory);

    builder.add("addOnsView", panel);

    builder.addRepeat("addOnSelectors", addOnService.getAddOns(), new AddOnSelectorFactory());

    editor = builder.add("editor", new JEditorPane()).getComponent();
    editor.addHyperlinkListener(new HyperlinkHandler(directory, directory.get(JFrame.class)));
    initHtmlEditor(editor);

    builder.add("image", imageLabel);

    cards = builder.addCardHandler("cards");

    builder.add("buy", new BuyAddOnAction());
    builder.add("gotoDemoAccount", new ShowInDemoAccountAction(directory));

    parentBuilder.add("addOnsView", builder);

    builder.addOnLoadListener(new OnLoadListener() {
      public void processLoad() {
        group.getElements().nextElement().doClick();
      }
    });
  }

  public static void initHtmlEditor(JEditorPane editor) {
    GuiUtils.initHtmlComponent(editor);
    GuiUtils.loadCssResource("/addons/addons.css", editor, AddOnsView.class);
  }

  private class AddOnSelectorFactory implements RepeatComponentFactory<AddOn> {

    public void registerComponents(PanelBuilder cellBuilder, final AddOn addOn) {
      JToggleButton toggle = new JToggleButton(addOn.getName());
      toggle.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          select(addOn);
        }
      });
      final SplitsNode<JToggleButton> addOnNode = cellBuilder.add("addOnSelector", toggle);
      group.add(toggle);

      final KeyChangeListener updater = new KeyChangeListener(AddOns.KEY) {
        public void update() {
          boolean enabled = addOn.isEnabled(repository);
          addOnNode.applyStyle(enabled ? "addOnEnabled" : "addOnDisabled");
          if (addOn == selectedAddOn) {
            cards.show(enabled ? "enabled" : "disabled");
          }
        }
      };
      repository.addChangeListener(updater);
      cellBuilder.addDisposable(new Disposable() {
        public void dispose() {
          repository.removeChangeListener(updater);
        }
      });
      updater.update();
    }
  }

  private void select(AddOn addOn) {
    this.selectedAddOn = addOn;
    imageLabel.setIcon(addOn.getIcon());
    editor.setText(addOn.getDescription());
    editor.setCaretPosition(0);
    cards.show(addOn.isEnabled(repository) ? "enabled" : "disabled");
  }

  private class BuyAddOnAction extends AbstractAction {
    public BuyAddOnAction() {
      super(Lang.get("addons.buy"));
    }

    public void actionPerformed(ActionEvent e) {
      selectedAddOn.activate(repository, directory);
    }
  }

  public class ShowInDemoAccountAction extends AbstractAction {

    private Directory directory;

    public ShowInDemoAccountAction(Directory directory) {
      super(Lang.get("signpostView.gotoDemo.button"));
      this.directory = directory;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      directory.get(LogoutService.class).gotoDemoAccount();
    }
  }}
