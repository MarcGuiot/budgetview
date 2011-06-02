package org.designup.picsou.gui.transactions.actions;

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
    JFrame parent = directory.get(JFrame.class);
    GlobList transactions = selectionService.getSelection(Transaction.TYPE);
    DeleteTransactionDialog dialog =
      new DeleteTransactionDialog(transactions, parent, repository, directory);
    dialog.show();
  }
}
