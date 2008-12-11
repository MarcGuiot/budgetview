package org.designup.picsou.gui.card;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.help.HelpAction;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class NavigationView extends View {

  private static Card[] CARDS = {Card.BUDGET, Card.SERIES_EVOLUTION, Card.DATA, Card.CATEGORIZATION};

  public NavigationView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/navigationView.splits",
                                                      repository, directory);

    final ImageLocator images = directory.get(ImageLocator.class);

    builder.add("importFileAction",
                ImportFileAction.init(Lang.get("navigationView.import.title"), repository, directory, null));

    builder.add("openHelpAction", new HelpAction(Lang.get("navigationView.help.title"), "index", directory));

    builder.addRepeat("cards", Arrays.asList(CARDS), new RepeatComponentFactory<Card>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, Card card) {

        Action action = new GotoCardAction(card);

        JButton iconButton = cellBuilder.add("icon", new JButton(action));
        Gui.configureIconButton(iconButton, card.getName(), NavigationIcons.DIMENSION);
        iconButton.setIcon(NavigationIcons.getLarge(images, card));
        iconButton.setRolloverIcon(NavigationIcons.getLargeWithRollover(images, card));

        cellBuilder.add("gotoCardAction", action);

        JTextArea textArea = cellBuilder.add("text", new JTextArea());
        textArea.setText(card.getDescription());
      }
    });

    parentBuilder.add("navigationView", builder);
  }

  private class GotoCardAction extends AbstractAction {

    private Card card;

    private GotoCardAction(Card card) {
      super(card.getLabel());
      this.card = card;
    }

    public void actionPerformed(ActionEvent e) {
      directory.get(NavigationService.class).gotoCard(card);
    }
  }
}
