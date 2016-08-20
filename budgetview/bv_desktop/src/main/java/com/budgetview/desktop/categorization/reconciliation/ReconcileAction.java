package com.budgetview.desktop.categorization.reconciliation;

import com.budgetview.model.Transaction;
import com.budgetview.utils.Lang;
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

import static org.globsframework.model.FieldValue.value;

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
                          value(Transaction.SERIES, toReconcile.get(Transaction.SERIES)),
                          value(Transaction.SUB_SERIES, toReconcile.get(Transaction.SUB_SERIES)),
                          value(Transaction.LABEL, toReconcile.get(Transaction.LABEL)));
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
