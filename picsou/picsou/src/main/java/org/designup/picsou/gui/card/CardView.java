package org.designup.picsou.gui.card;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.gui.transactions.TransactionSelection;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CardView extends View implements GlobSelectionListener {
  private CardHandler handler;
  private Card lastSelectedCard = Card.DATA;
  private TransactionSelection transactionSelection;

  public CardView(GlobRepository repository, Directory directory, TransactionSelection transactionSelection) {
    super(repository, directory);
    this.transactionSelection = transactionSelection;
    this.transactionSelection.addListener(this);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    handler = builder.addCardHandler("cardView");
    handler.show(lastSelectedCard.getName());

    JToggleButton dataCardToggle = new JToggleButton(new ToggleAction(Card.DATA));
    builder.add("dataCardToggle", dataCardToggle);

    JToggleButton overviewCardToggle = new JToggleButton(new ToggleAction(Card.OVERVIEW));
    builder.add("overviewCardToggle", overviewCardToggle);

    ButtonGroup group = new ButtonGroup();
    group.add(dataCardToggle);
    group.add(overviewCardToggle);

    dataCardToggle.setSelected(true);

    JTextArea textArea = new JTextArea();
    textArea.setText(Lang.get("noData"));
    builder.add("noData", textArea);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (hasData(transactionSelection.getSelectedMonthStats())) {
      handler.show(lastSelectedCard.getName());
    }
    else {
      handler.show("noData");
    }
  }

  private boolean hasData(GlobList monthStats) {
    for (Glob monthStat : monthStats) {
      if ((monthStat.get(MonthStat.EXPENSES) != 0.0) || (monthStat.get(MonthStat.INCOME) != 0.0)) {
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
      lastSelectedCard = card;
      handler.show(lastSelectedCard.getName());
    }
  }

  private enum Card {
    DATA, OVERVIEW;

    String getName() {
      return name().toLowerCase();
    }

    String getLabel() {
      return Lang.get("cards." + getName());
    }
  }
}

