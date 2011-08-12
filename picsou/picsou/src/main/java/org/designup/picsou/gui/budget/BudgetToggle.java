package org.designup.picsou.gui.budget;

import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;

public class BudgetToggle {

  private CardHandler cards;

  public void showMain() {
    cards.show("main");
  }

  public void showSavings() {
    cards.show("savings");
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    cards = builder.addCardHandler("budgetToggle");
    cards.show("main");
  }
}
