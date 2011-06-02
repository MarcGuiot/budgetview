package org.designup.picsou.gui.transactions.actions;

import org.designup.picsou.gui.categorization.actions.CategorizeTransactionsAction;
import org.designup.picsou.gui.transactions.shift.ShiftTransactionAction;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class TransactionTableActions implements PopupMenuFactory {

  private CategorizeTransactionsAction categorize;
  private ShiftTransactionAction shift;
  private DeleteTransactionAction delete;

  public TransactionTableActions(GlobRepository repository, Directory directory) {

    this.categorize = new CategorizeTransactionsAction(directory);
    this.shift = new ShiftTransactionAction(repository, directory);
    this.delete = new DeleteTransactionAction(repository, directory);
  }

  public ShiftTransactionAction getShift() {
    return shift;
  }

  public DeleteTransactionAction getDelete() {
    return delete;
  }

  public JPopupMenu createPopup() {
    JPopupMenu popup = new JPopupMenu();
    popup.add(categorize);
    popup.add(shift);
    popup.add(delete);
    return popup;
  }
}
