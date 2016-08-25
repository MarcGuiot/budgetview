package com.budgetview.desktop.budget;

import com.budgetview.desktop.View;
import com.budgetview.model.Account;
import com.budgetview.shared.model.AccountType;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.OnLoadListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class BudgetToggle extends View implements GlobSelectionListener {

  private CardHandler cards;

  public BudgetToggle(GlobRepository repository, Directory directory) {
    super(repository, directory);
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
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    cards = builder.addCardHandler("budgetToggle");
    builder.addOnLoadListener(new OnLoadListener() {
      public void processLoad() {
        showMain();
      }
    });
  }

}
