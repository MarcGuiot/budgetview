package org.designup.picsou.gui.transactions;

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

public class SplitTransactionAction extends AbstractAction implements GlobSelectionListener {

  private Glob selectedTransaction;
  private GlobRepository repository;
  private Directory directory;

  public SplitTransactionAction(GlobRepository repository, Directory directory) {
    super(Lang.get("split.transaction"));
    this.repository = repository;
    this.directory = directory;
    directory.get(SelectionService.class).addListener(this, Transaction.TYPE);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (!selection.isRelevantForType(Transaction.TYPE)) {
      return;
    }
    GlobList transactions = selection.getAll(Transaction.TYPE);
    selectedTransaction = transactions.size() == 1 ? transactions.get(0) : null;
    setEnabled(selectedTransaction != null);
  }

  public void actionPerformed(ActionEvent e) {
    SplitTransactionDialog dialog = new SplitTransactionDialog(selectedTransaction, repository, directory);
    dialog.show();
  }
}
