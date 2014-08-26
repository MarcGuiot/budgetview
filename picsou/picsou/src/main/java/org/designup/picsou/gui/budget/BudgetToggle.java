package org.designup.picsou.gui.budget;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.OnLoadListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class BudgetToggle implements GlobSelectionListener {

  private CardHandler cards;
  private GlobRepository repository;

  public BudgetToggle(GlobRepository repository, Directory directory) {
    this.repository = repository;
    directory.get(SelectionService.class).addListener(this, Account.TYPE);
  }

  public void selectionUpdated(GlobSelection selection) {
    Set<Integer> types = selection.getAll(Account.TYPE).getValueSet(Account.ACCOUNT_TYPE);
    if (types.size() != 1 || AccountType.MAIN.getId().equals(types.iterator().next())) {
      showMain();
    }
    else {
      showSavings();
    }
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
    builder.addOnLoadListener(new OnLoadListener() {
      public void processLoad() {
        cards.show("main");
      }
    });
  }
}
