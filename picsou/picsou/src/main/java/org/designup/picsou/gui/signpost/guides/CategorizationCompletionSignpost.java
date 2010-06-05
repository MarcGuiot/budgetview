package org.designup.picsou.gui.signpost.guides;

import org.designup.picsou.gui.categorization.components.CategorizationLevel;
import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobList;
import org.globsframework.utils.Updatable;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobSelection;

public class CategorizationCompletionSignpost extends Signpost implements Updatable {
  private CategorizationLevel categorizationLevel;

  public CategorizationCompletionSignpost(CategorizationLevel categorizationLevel,
                                          GlobRepository repository,
                                          Directory directory) {
    super(SignpostStatus.CATEGORIZATION_COMPLETION_SHOWN, repository, directory);
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
}
