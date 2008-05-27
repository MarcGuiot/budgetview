package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;

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
