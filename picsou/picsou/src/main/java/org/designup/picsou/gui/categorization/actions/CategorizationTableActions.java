package org.designup.picsou.gui.categorization.actions;

import org.designup.picsou.gui.transactions.actions.DeleteTransactionAction;
import org.designup.picsou.gui.transactions.actions.EditTransactionAction;
import org.designup.picsou.gui.transactions.reconciliation.annotations.AnnotateReconciledTransactionAction;
import org.designup.picsou.gui.transactions.shift.ShiftTransactionAction;
import org.designup.picsou.gui.transactions.split.SplitTransactionAction;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class CategorizationTableActions implements PopupMenuFactory {

  private GlobRepository repository;
  private EditTransactionAction edit;
  private SplitTransactionAction split;
  private ShiftTransactionAction shift;
  private AnnotateReconciledTransactionAction reconcile;
  private DeleteTransactionAction delete;
  private Action copy;

  public CategorizationTableActions(Action copy, GlobRepository repository, Directory directory) {
    this.copy = copy;
    this.repository = repository;
    this.edit = new EditTransactionAction(repository, directory);
    this.split = new SplitTransactionAction(repository, directory);
    this.shift = new ShiftTransactionAction(repository, directory);
    this.reconcile = new AnnotateReconciledTransactionAction(repository, directory);
    this.delete = new DeleteTransactionAction(repository, directory);
  }

  public DeleteTransactionAction getDelete() {
    return delete;
  }

  public JPopupMenu createPopup() {
    JPopupMenu popup = new JPopupMenu();
    addPopupActions(popup, true);
    return popup;
  }

  public void addPopupActions(JPopupMenu popup, boolean addSeparators) {
    popup.add(edit);
    popup.add(split);
    popup.add(shift);
    if (UserPreferences.isReconciliationShown(repository)) {
      popup.add(reconcile);
    }
    if (addSeparators) {
      popup.addSeparator();
    }
    popup.add(copy);
    if (addSeparators) {
      popup.addSeparator();
    }
    popup.add(delete);
  }

  public JPopupMenu createEditPopup() {
    JPopupMenu popup = new JPopupMenu();
    popup.add(edit);
    popup.add(split);
    popup.add(shift);
    if (UserPreferences.isReconciliationShown(repository)) {
      popup.add(reconcile);
    }
    popup.add(delete);
    return popup;
  }
}
