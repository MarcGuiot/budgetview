package org.designup.picsou.gui.card;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CardView extends View implements GlobSelectionListener {
  private CardHandler mainCards;
  private CardHandler analysisCards;

  private Card lastSelectedMasterCard = Card.HOME;
  private Card lastSelectedSubCard = Card.DATA;
  private JToggleButton[] toggles = new JToggleButton[Card.values().length];

  public CardView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.selectionService.addListener(this, Card.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder builder) {

    mainCards = builder.addCardHandler("mainCards");
    analysisCards = builder.addCardHandler("analysisCards");

    ButtonGroup masterGroup = new ButtonGroup();
    ButtonGroup secondaryGroup = new ButtonGroup();
    for (Card card : Card.values()) {
      JToggleButton toggle = new JToggleButton(new ToggleAction(card));
      toggle.setToolTipText(card.getLabel());
      String name = card.getName() + "CardToggle";
      Gui.configureIconButton(toggle, name, new Dimension(45, 45));
      builder.add(name, toggle);
      if (card.isMaster()) {
        masterGroup.add(toggle);
      }
      else {
        secondaryGroup.add(toggle);
      }
      toggles[card.getId()] = toggle;
    }

    showCard(Card.HOME);
  }

  private void showCard(Card card) {
    if (card.isMaster()) {
      toggles[card.getId()].doClick(0);
    }
    else {
      toggles[card.getMasterCard().getId()].doClick(0);
      toggles[card.getId()].doClick(0);
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Card.TYPE)) {
      GlobList cards = selection.getAll(Card.TYPE);
      if (cards.size() == 1) {
        showCard(Card.get(cards.get(0).get(Card.ID)));
      }
    }
    if (selection.isRelevantForType(Month.TYPE) || selection.isRelevantForType(Category.TYPE)) {
      showCard(lastSelectedMasterCard);
    }
  }

  private class ToggleAction extends AbstractAction {
    private Card card;

    public ToggleAction(Card card) {
      super(card.getLabel());
      this.card = card;
    }

    public void actionPerformed(ActionEvent e) {
      if (card == lastSelectedMasterCard) {
        return;
      }

      if (card.isMaster()) {
        lastSelectedMasterCard = card;
      }
      else {
        if ((card.getMasterCard() == lastSelectedMasterCard) && (card == lastSelectedSubCard)) {
          return;
        }
        lastSelectedMasterCard = card.getMasterCard();
        lastSelectedSubCard = card;
      }

      if (card.isMaster()) {
        mainCards.show(card.getName());
      }
      else {
        analysisCards.show(card.getName());
        mainCards.show(card.getMasterCard().getName());
      }

      if (card.containsSubCards()) {
        selectionService.select(repository.get(lastSelectedSubCard.getKey()));
      }
      else {
        selectionService.select(repository.get(card.getKey()));
      }
    }
  }
}

