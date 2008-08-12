package org.designup.picsou.gui.card;

import org.designup.picsou.gui.TransactionSelection;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Category;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.model.Glob;
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
  private TransactionSelection transactionSelection;

  public CardView(GlobRepository repository, Directory directory, TransactionSelection transactionSelection) {
    super(repository, directory);
    this.transactionSelection = transactionSelection;
    this.transactionSelection.addListener(this);
    this.selectionService.addListener(this, Card.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder builder) {

    masterCardHandler = builder.addCardHandler("cardView");
    categoryCardHandler = builder.addCardHandler("cardsWithCategoriesView");

    showCard(lastSelectedCard);

    ButtonGroup group = new ButtonGroup();
    JToggleButton[] toggles = new JToggleButton[Card.values().length];
    for (int i = 0; i < Card.values().length; i++) {
      Card card = Card.values()[i];
      JToggleButton toggle = new JToggleButton(new ToggleAction(card));
      String name = card.getName() + "CardToggle";
      Gui.configureIconButton(toggle, name, new Dimension(45, 45));
      builder.add(name, toggle);
      group.add(toggle);
      toggles[i] = toggle;
    }

    toggles[0].setSelected(true);

    JTextArea textArea = new JTextArea();
    textArea.setText(Lang.get("noData"));
    builder.add("noData", textArea);
  }

  private void showCard(Card card) {
    if (card.showCategoryCard) {
      if (hasData(transactionSelection.getSelectedMonthStats())) {
        categoryCardHandler.show(card.getName());
        masterCardHandler.show("withCategories");
      }
      else {
        categoryCardHandler.show("noData");
        masterCardHandler.show("withCategories");
      }
    }
    else {
      masterCardHandler.show(card.getName());
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Card.TYPE)) {
      GlobList cards = selection.getAll(Card.TYPE);
      if (cards.size() == 1) {
        lastSelectedCard = Card.get(cards.get(0).get(Card.ID));
        showCard(lastSelectedCard);
      }
    }
    if (selection.isRelevantForType(Month.TYPE) || selection.isRelevantForType(Category.TYPE)) {
      showCard(lastSelectedCard);
    }
  }

  private boolean hasData(GlobList monthStats) {
    for (Glob monthStat : monthStats) {
      if ((monthStat.get(MonthStat.TOTAL_SPENT) != 0.0)
          || (monthStat.get(MonthStat.TOTAL_RECEIVED) != 0.0)
          || monthStat.get(MonthStat.PLANNED_TOTAL_RECEIVED) != 0.0
          || monthStat.get(MonthStat.PLANNED_TOTAL_SPENT) != 0.0) {
        return true;
      }
    }
    return false;
  }

  private class ToggleAction extends AbstractAction {
    private Card card;

    public ToggleAction(Card card) {
      super(card.getLabel());
      this.card = card;
    }

    public void actionPerformed(ActionEvent e) {
      selectionService.select(repository.get(card.getKey()));
    }
  }
}

