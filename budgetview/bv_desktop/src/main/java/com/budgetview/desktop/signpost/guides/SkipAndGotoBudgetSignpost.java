package com.budgetview.desktop.signpost.guides;

import com.budgetview.desktop.model.Card;
import com.budgetview.desktop.signpost.SimpleSignpost;
import com.budgetview.model.SignpostStatus;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SkipAndGotoBudgetSignpost extends SimpleSignpost implements GlobSelectionListener {
  public SkipAndGotoBudgetSignpost(GlobRepository repository, Directory directory) {
    super(Lang.get("signpost.skipAndGotoBudget"),
          SignpostStatus.GOTO_BUDGET_DONE,
          SignpostStatus.CATEGORIZATION_SKIPPED,
          repository, directory);
  }

  protected void init() {
    super.init();
    selectionService.addListener(this, Card.TYPE);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (!isShowing()) {
      return;
    }
    GlobList cards = selection.getAll(Card.TYPE);
    if (cards.size() != 1) {
      return;
    }
    if (Card.BUDGET.getId() == cards.get(0).get(Card.ID)) {
      dispose();
      SignpostStatus.setAllBeforeBudgetCompleted(repository);
    }
  }
}