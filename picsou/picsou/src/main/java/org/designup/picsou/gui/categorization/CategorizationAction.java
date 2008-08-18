package org.designup.picsou.gui.categorization;

import org.designup.picsou.model.Transaction;
import org.globsframework.gui.actions.AbstractGlobSelectionAction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import java.awt.event.ActionEvent;

public abstract class CategorizationAction extends AbstractGlobSelectionAction {
  private GlobRepository repository;

  public CategorizationAction(GlobRepository repository, Directory directory) {
    super(Transaction.TYPE, directory);
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    CategorizationDialog dialog = directory.get(CategorizationDialog.class);
    dialog.show(getTransactionList(), true, false);
  }

  private GlobList getTransactionList() {
    if (lastSelection.size() > 1) {
      return lastSelection;
    }
    Glob transaction = lastSelection.get(0);
    if ((transaction.get(Transaction.SERIES) != null) ||
        (Strings.isNullOrEmpty(transaction.get(Transaction.LABEL_FOR_CATEGORISATION)))) {
      return new GlobList(transaction);
    }
    else {
      return getAllGlobs()
        .filter(and(fieldEquals(Transaction.LABEL_FOR_CATEGORISATION,
                                transaction.get(Transaction.LABEL_FOR_CATEGORISATION)),
                    isNull(Transaction.SERIES)),
                repository);
    }

  }

  protected abstract GlobList getAllGlobs();

  public String toString(GlobList globs) {
    return "categorize";
  }
}
