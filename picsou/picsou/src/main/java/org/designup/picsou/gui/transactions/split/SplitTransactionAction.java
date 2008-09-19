package org.designup.picsou.gui.transactions.split;

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
  private SplitTransactionDialog dialog;

  public SplitTransactionAction(GlobRepository repository, Directory directory) {
    super(Lang.get("split.transaction.new"));
    dialog = new SplitTransactionDialog(repository, directory);
    directory.get(SelectionService.class).addListener(this, Transaction.TYPE);
    setEnabled(false);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (!selection.isRelevantForType(Transaction.TYPE)) {
      return;
    }

    GlobList transactions = selection.getAll(Transaction.TYPE);
    selectedTransaction = transactions.size() == 1 ? transactions.get(0) : null;
    setEnabled(selectedTransaction != null);

    this.putValue(Action.NAME, getLabel(selectedTransaction));
  }

  private String getLabel(Glob transaction) {
    if (transaction != null &&
        (Boolean.TRUE.equals(transaction.get(Transaction.SPLIT))
         || (transaction.get(Transaction.SPLIT_SOURCE) != null))) {
      return Lang.get("split.transaction.existing");
    }
    return Lang.get("split.transaction.new");
  }

  public void actionPerformed(ActionEvent e) {
    dialog.show(selectedTransaction);
  }
}
