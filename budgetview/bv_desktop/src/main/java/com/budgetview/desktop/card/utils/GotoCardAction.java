package com.budgetview.desktop.card.utils;

import com.budgetview.desktop.card.NavigationService;
import com.budgetview.desktop.model.Card;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GotoCardAction extends AbstractAction {

  private Card card;
  private Directory directory;

  public GotoCardAction(String text, Card card, Directory directory) {
    super(text);
    this.card = card;
    this.directory = directory;
  }

  public GotoCardAction(Card card, Directory directory) {
    this(card.getLabel(), card, directory);
  }

  public void actionPerformed(ActionEvent e) {
    directory.get(NavigationService.class).gotoCard(card);
  }
}
