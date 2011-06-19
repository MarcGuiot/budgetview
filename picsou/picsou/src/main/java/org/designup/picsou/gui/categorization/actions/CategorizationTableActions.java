package org.designup.picsou.gui.categorization.actions;

import org.designup.picsou.gui.transactions.actions.DeleteTransactionAction;
import org.designup.picsou.gui.transactions.reconciliation.ReconcileTransactionAction;
import org.designup.picsou.gui.transactions.shift.ShiftTransactionAction;
import org.designup.picsou.gui.transactions.split.SplitTransactionAction;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class CategorizationTableActions implements PopupMenuFactory {

  private GlobRepository repository;
  private SplitTransactionAction split;
  private ShiftTransactionAction shift;
  private ReconcileTransactionAction reconcile;
  private DeleteTransactionAction delete;
  private Action copy;

  public CategorizationTableActions(Action copy, GlobRepository repository, Directory directory) {
    this.copy = copy;
    this.repository = repository;
    this.split = new SplitTransactionAction(repository, directory);
    this.shift = new ShiftTransactionAction(repository, directory);
    this.reconcile = new ReconcileTransactionAction(repository, directory);
    this.delete = new DeleteTransactionAction(repository, directory);
  }

  public ShiftTransactionAction getShift() {
    return shift;
  }

  public SplitTransactionAction getSplit() {
    return split;
  }

  public DeleteTransactionAction getDelete() {
    return delete;
  }

  public JPopupMenu createPopup() {
    JPopupMenu popup = new JPopupMenu();
    popup.add(split);
    popup.add(shift);

    if (UserPreferences.isReconciliationShown(repository)) {
      popup.add(reconcile);
    }
    popup.addSeparator();
    popup.add(copy);
    popup.addSeparator();
    popup.add(delete);
    return popup;
  }
}