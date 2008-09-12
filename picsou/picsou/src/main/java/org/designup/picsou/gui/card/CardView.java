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
  private CardHandler masterCardHandler;
  private CardHandler categoryCardHandler;

  private Card lastSelectedCard = Card.HOME;
  private JToggleButton[] toggles = new JToggleButton[Card.values().length];

  public CardView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.selectionService.addListener(this, Card.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder builder) {

    masterCardHandler = builder.addCardHandler("cardView");
    categoryCardHandler = builder.addCardHandler("cardsWithCategoriesView");

    ButtonGroup group = new ButtonGroup();
    for (Card card : Card.values()) {
      JToggleButton toggle = new JToggleButton(new ToggleAction(card));
      String name = card.getName() + "CardToggle";
      Gui.configureIconButton(toggle, name, new Dimension(45, 45));
      builder.add(name, toggle);
      group.add(toggle);
      toggles[card.getId()] = toggle;
    }

    showCard(lastSelectedCard);
  }

  private void showCard(Card card) {
    toggles[card.getId()].doClick(0);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Card.TYPE)) {
      GlobList cards = selection.getAll(Card.TYPE);
      if (cards.size() == 1) {
        showCard(Card.get(cards.get(0).get(Card.ID)));
      }
    }
    if (selection.isRelevantForType(Month.TYPE) || selection.isRelevantForType(Category.TYPE)) {
      showCard(lastSelectedCard);
    }
  }

  private class ToggleAction extends AbstractAction {
    private Card card;

    public ToggleAction(Card card) {
      super(card.getLabel());
      this.card = card;
    }

    public void actionPerformed(ActionEvent e) {
      if (card == lastSelectedCard) {
        return;
      }

      lastSelectedCard = card;

      if (card.showCategoryCard) {
        categoryCardHandler.show(card.getName());
        masterCardHandler.show("withCategories");
      }
      else {
        masterCardHandler.show(card.getName());
      }

      selectionService.select(repository.get(card.getKey()));
    }
  }
}

