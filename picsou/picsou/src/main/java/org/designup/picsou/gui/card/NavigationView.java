package org.designup.picsou.gui.card;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.card.utils.GotoCardAction;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class NavigationView extends View {

  public NavigationView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/cards/navigationView.splits",
                                                      repository, directory);

    addComponents(builder, Card.DATA);
    addComponents(builder, Card.BUDGET);

    parentBuilder.add("navigationView", builder);
  }

  private void addComponents(GlobsPanelBuilder builder, Card card) {
    JButton button = new JButton(new GotoCardAction(Lang.get("navigationView." + card.getName().toLowerCase()), card, directory));
    String name = "goto" + Strings.capitalize(card.getName());
    builder.add(name, button);
    builder.add(name + "Icon", Gui.createSyncButton(button));
  }
}
