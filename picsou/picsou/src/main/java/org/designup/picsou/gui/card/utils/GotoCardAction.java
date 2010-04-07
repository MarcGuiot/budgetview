package org.designup.picsou.gui.card.utils;

import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.card.NavigationService;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GotoCardAction extends AbstractAction {

  private Card card;
  private Directory directory;

  public GotoCardAction(Card card, Directory directory) {
    super(card.getLabel());
    this.card = card;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    directory.get(NavigationService.class).gotoCard(card);
  }
}
