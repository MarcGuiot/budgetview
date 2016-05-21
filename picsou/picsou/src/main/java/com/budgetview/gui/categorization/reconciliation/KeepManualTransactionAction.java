package com.budgetview.gui.categorization.reconciliation;

import com.budgetview.model.Transaction;
import com.budgetview.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class KeepManualTransactionAction extends AbstractAction {

  private Glob transaction;
  private GlobRepository repository;

  public KeepManualTransactionAction(GlobRepository repository) {
    super(Lang.get("reconciliation.keepManual.action"));
    this.repository = repository;
    update();
  }
  
  public void setTransaction(Glob transaction) {
    this.transaction = transaction;
    update();
  }

  private void update() {
    setEnabled(transaction != null);
  }

  public void actionPerformed(ActionEvent actionEvent) {
    repository.update(transaction.getKey(), Transaction.TO_RECONCILE, false);
  }
}
