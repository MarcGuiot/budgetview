package com.budgetview.gui.signpost.guides;

import com.budgetview.gui.model.Card;
import com.budgetview.model.SignpostStatus;
import com.budgetview.model.Transaction;
import com.budgetview.gui.signpost.PersistentSignpost;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class GotoCategorizationSignpost extends PersistentSignpost implements GlobSelectionListener, ChangeSetListener {
  public GotoCategorizationSignpost(GlobRepository repository, Directory directory) {
    super(SignpostStatus.GOTO_CATEGORIZATION_DONE, repository, directory);
  }

  protected void init() {
    directory.get(SelectionService.class).addListener(this, Card.TYPE);
    repository.addChangeListener(this);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Transaction.TYPE)) {
      update();
    }
  }

  private void update() {
    if (canShow() && repository.contains(Transaction.TYPE)) {
      show(Lang.get("signpost.gotoCategorization"));
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    Glob card = selection.getAll(Card.TYPE).getFirst();
    if (isShowing() && (card != null && Card.get(card) == Card.CATEGORIZATION)) {
      dispose();
    }
  }
}
