package org.designup.picsou.gui.transactions.reconciliation;

import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ReconcileTransactionAction extends AbstractAction implements GlobSelectionListener {

  private GlobRepository repository;
  private SelectionService selectionService;

  public ReconcileTransactionAction(GlobRepository repository, Directory directory) {
    this.repository = repository;
    repository.addChangeListener(new TypeChangeSetListener(Transaction.TYPE) {
      protected void update(GlobRepository repository) {
        updateLabel();
      }
    });
    selectionService = directory.get(SelectionService.class);
    selectionService.addListener(this, Transaction.TYPE);
  }

  public void selectionUpdated(GlobSelection selection) {
    updateLabel();
  }

  private void updateLabel() {
    boolean reconcile = shouldReconcile(selectionService.getSelection(Transaction.TYPE));
    putValue(Action.NAME, Lang.get(reconcile ? "reconciliation.action.do" : "reconciliation.action.undo"));
  }

  public void actionPerformed(ActionEvent actionEvent) {
    boolean reconcile = shouldReconcile(selectionService.getSelection(Transaction.TYPE));
    try {
      repository.startChangeSet();
      for (Glob transaction : selectionService.getSelection(Transaction.TYPE)) {
        repository.update(transaction.getKey(), Transaction.RECONCILED, reconcile);
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private boolean shouldReconcile(GlobList transactions) {
    for (Glob transaction : transactions) {
      if (!transaction.isTrue(Transaction.RECONCILED)) {
        return true;
      }
    }
    return false;
  }
}
