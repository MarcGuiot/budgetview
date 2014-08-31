package org.designup.picsou.gui.signpost.guides;

import org.designup.picsou.gui.categorization.components.CategorizationLevel;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.signpost.PersistentSignpost;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Updatable;
import org.globsframework.utils.directory.Directory;

public class GotoBudgetSignpost extends PersistentSignpost implements Updatable {
  private CategorizationLevel categorizationLevel;

  public GotoBudgetSignpost(CategorizationLevel categorizationLevel,
                            GlobRepository repository,
                            Directory directory) {
    super(SignpostStatus.GOTO_BUDGET_DONE, repository, directory);
    this.categorizationLevel = categorizationLevel;
  }

  protected void init() {
    categorizationLevel.addListener(this);
    selectionService.addListener(new GlobSelectionListener() {
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
        }
      }
    }, Card.TYPE);
  }

  public void update() {
    if (!SignpostStatus.isCompleted(SignpostStatus.FIRST_CATEGORIZATION_DONE, repository) ||
        SignpostStatus.isCompleted(SignpostStatus.CREATED_TRANSACTIONS_MANUALLY, repository)) {
      return;
    }

    double total = categorizationLevel.getTotal();
    double percentage = categorizationLevel.getPercentage();
    if ((total == 0) && isShowing()) {
      dispose();
    }
    else if ((percentage == 0) && canShow()) {
      show(Lang.get("signpost.categorizationCompletion.full"));
    }
    else if ((percentage <= 0.1) && canShow()) {
      show(Lang.get("signpost.categorizationCompletion.quasi"));
    }
    else if (isShowing()) {
      dispose();
    }
  }

  protected void show(String text) {
    SignpostStatus.setCompleted(SignpostStatus.GOTO_BUDGET_SHOWN, repository);
    super.show(text);
  }
}
