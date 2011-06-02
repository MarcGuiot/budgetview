package org.designup.picsou.gui.transactions.actions;

import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.transactions.DeleteTransactionDialog;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;

public class DeleteTransactionAction extends AbstractAction implements GlobSelectionListener {

  private GlobRepository repository;
  private Directory directory;
  private SelectionService selectionService;

  public DeleteTransactionAction(GlobRepository repository, Directory directory) {
    super(Lang.get("transaction.delete.action"));
    this.repository = repository;
    this.directory = directory;

    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(this, Transaction.TYPE);
  }

  public void selectionUpdated(GlobSelection selection) {
    setEnabled(!selection.getAll(Transaction.TYPE).isEmpty());
  }

  public void actionPerformed(ActionEvent actionEvent) {
    GlobList transactions = selectionService.getSelection(Transaction.TYPE);

    if (hasPlanned(transactions)) {
      MessageDialog.show("transaction.delete.title.forbidden", directory,
                         "transaction.delete.planned");
      return;
    }

    if (hasAutoCreated(transactions)) {
      MessageDialog.show("transaction.delete.title.forbidden", directory,
                         "transaction.delete.auto");
      return;
    }

    DeleteTransactionDialog dialog =
      new DeleteTransactionDialog(transactions, repository, directory);
    dialog.show();
  }

  private boolean hasPlanned(GlobList transactions) {
    boolean hasPlanned = false;
    for (Iterator<Glob> iterator = transactions.iterator(); iterator.hasNext(); ) {
      Glob glob = iterator.next();
      if (Transaction.isPlanned(glob)) {
        iterator.remove();
        hasPlanned = true;
      }
    }
    return hasPlanned;
  }

  private boolean hasAutoCreated(GlobList transactions) {
    boolean hasAutoCreated = false;
    for (Iterator<Glob> iterator = transactions.iterator(); iterator.hasNext(); ) {
      Glob glob = iterator.next();
      if (Transaction.isCreatedBySeries(glob) || Transaction.isMirrorTransaction(glob)) {
        iterator.remove();
        hasAutoCreated = true;
      }
    }
    return hasAutoCreated;
  }
}
