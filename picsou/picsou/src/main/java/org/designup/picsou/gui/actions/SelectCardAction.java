package org.designup.picsou.gui.actions;

import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;

public class SelectCardAction extends AbstractAction {

  private CardHandler cards;
  private String card;

  public SelectCardAction(String label, CardHandler cards, String card) {
    super(label);
    this.cards = cards;
    this.card = card;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    cards.show(card);
    postSelect();
  }

  protected void postSelect() {

  }
}
