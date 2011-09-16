package org.designup.picsou.gui.budget;

import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.model.GlobRepository;

public class BudgetToggle {

  private CardHandler cards;
  private GlobRepository repository;

  public BudgetToggle(GlobRepository repository) {
    this.repository = repository;
  }

  public void showMain() {
    cards.show("main");
  }

  public void showSavings() {
    cards.show("savings");
    SignpostStatus.setCompleted(SignpostStatus.SAVINGS_VIEW_TOGGLE_SHOWN, repository);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    cards = builder.addCardHandler("budgetToggle");
    cards.show("main");
  }
}
