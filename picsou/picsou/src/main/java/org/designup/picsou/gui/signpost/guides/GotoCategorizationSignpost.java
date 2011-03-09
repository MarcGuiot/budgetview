package org.designup.picsou.gui.signpost.guides;

import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.gui.signpost.SimpleSignpost;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
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

public class GotoCategorizationSignpost extends Signpost implements GlobSelectionListener, ChangeSetListener {
  public GotoCategorizationSignpost(GlobRepository repository, Directory directory) {
    super(SignpostStatus.GOTO_CATEGORIZATION_SHOWN, repository, directory);
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

  public void dispose() {
    directory.get(SelectionService.class).removeListener(this);
    repository.removeChangeListener(this);
    super.dispose();
  }
}
