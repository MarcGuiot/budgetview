package org.designup.picsou.gui.transactions.reconciliation.annotations;

import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.actions.MultiSelectionAction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class AnnotateReconciledTransactionAction extends MultiSelectionAction {

  public AnnotateReconciledTransactionAction(GlobRepository repository, Directory directory) {
    super("", Transaction.TYPE, repository, directory);
    repository.addChangeListener(new TypeChangeSetListener(Transaction.TYPE) {
      protected void update(GlobRepository repository) {
        updateLabel();
      }
    });
  }

  protected void processSelection(GlobList selection) {
    updateLabel();
  }

  private void updateLabel() {
    boolean reconcile = shouldReconcile(selectionService.getSelection(Transaction.TYPE));
    putValue(Action.NAME, Lang.get(reconcile ? "reconciliation.annotation.action.do" : "reconciliation.annotation.action.undo"));
  }

  protected void processClick(GlobList transactions, GlobRepository repository, Directory directory) {
    boolean reconcile = shouldReconcile(transactions);
    try {
      repository.startChangeSet();
      for (Glob transaction : selectionService.getSelection(Transaction.TYPE)) {
        repository.update(transaction.getKey(), Transaction.RECONCILIATION_ANNOTATION_SET, reconcile);
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private boolean shouldReconcile(GlobList transactions) {
    for (Glob transaction : transactions) {
      if (!transaction.isTrue(Transaction.RECONCILIATION_ANNOTATION_SET)) {
        return true;
      }
    }
    return false;
  }
}
