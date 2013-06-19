package org.designup.picsou.gui.transactions.actions;

import org.designup.picsou.gui.categorization.actions.CategorizeTransactionsAction;
import org.designup.picsou.gui.transactions.shift.ShiftTransactionAction;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class TransactionTableActions implements PopupMenuFactory {

  private final EditTransactionAction edit;
  private final CategorizeTransactionsAction categorize;
  private final ShiftTransactionAction shift;
  private final DeleteTransactionAction delete;
  private final Action copy;
  private final EditTransactionSeriesAction editSeries;

  public TransactionTableActions(Action copy, GlobRepository repository, Directory directory) {
    this.copy = copy;
    this.edit = new EditTransactionAction(repository, directory);
    this.categorize = new CategorizeTransactionsAction(directory);
    this.shift = new ShiftTransactionAction(repository, directory);
    this.delete = new DeleteTransactionAction(repository, directory);
    this.editSeries = new EditTransactionSeriesAction(repository, directory);
  }

  public DeleteTransactionAction getDelete() {
    return delete;
  }

  public JPopupMenu createPopup() {
    JPopupMenu popup = new JPopupMenu();
    popup.add(edit);
    popup.add(categorize);
    popup.add(editSeries);
    popup.add(shift);
    popup.addSeparator();
    popup.add(copy);
    popup.addSeparator();
    popup.add(delete);
    return popup;
  }
}
