package org.designup.picsou.gui.categorization.reconciliation;

import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ReconcileAction extends AbstractAction implements GlobSelectionListener {

  private GlobRepository repository;
  private Glob toReconcile;
  private Glob targetTransaction;

  public ReconcileAction(GlobRepository repository, Directory directory) {
    super(Lang.get("reconciliation.reconcile.action"));
    this.repository = repository;
    directory.get(SelectionService.class).addListener(this, Transaction.TYPE);
  }

  public void setTransactionToReconcile(Glob transaction) {
    this.toReconcile = transaction;
    update();
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList transactions = selection.getAll(Transaction.TYPE);
    targetTransaction = transactions.size() > 0 ? transactions.getFirst() : null;
    update();
  }

  private void update() {
    setEnabled((toReconcile != null) && (targetTransaction != null));
  }

  public void actionPerformed(ActionEvent actionEvent) {
    repository.startChangeSet();
    try {
      repository.update(targetTransaction.getKey(),
                        Transaction.NOTE,
                        Strings.join(targetTransaction.get(Transaction.NOTE),
                                     toReconcile.get(Transaction.NOTE)));
      if (Transaction.isCategorized(toReconcile)) {
        repository.update(targetTransaction.getKey(),
                          FieldValue.value(Transaction.SERIES, toReconcile.get(Transaction.SERIES)),
                          FieldValue.value(Transaction.SUB_SERIES, toReconcile.get(Transaction.SUB_SERIES)),
                          FieldValue.value(Transaction.LABEL, toReconcile.get(Transaction.LABEL)));
      }
      repository.delete(toReconcile);
      toReconcile = null;
      targetTransaction = null;
      update();
    }
    finally {
      repository.completeChangeSet();
    }
  }
}
