package com.budgetview.desktop.transactions.split;

import com.budgetview.model.Transaction;
import com.budgetview.utils.Lang;
import org.globsframework.gui.actions.SingleSelectionAction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class SplitTransactionAction extends SingleSelectionAction {

  public SplitTransactionAction(GlobRepository repository, Directory directory) {
    super(Lang.get("split.transaction.new"), Transaction.TYPE, repository, directory);
  }

  protected void processSelection(Glob selectedTransaction) {
    this.putValue(Action.NAME, getLabel(selectedTransaction));
  }

  private String getLabel(Glob transaction) {
    if (transaction != null &&
        (transaction.isTrue(Transaction.SPLIT)
         || (transaction.get(Transaction.SPLIT_SOURCE) != null))) {
      return Lang.get("split.transaction.existing");
    }
    return Lang.get("split.transaction.new");
  }

  protected void process(Glob selectedTransaction, GlobRepository repository, Directory directory) {
    SplitTransactionDialog dialog = new SplitTransactionDialog(repository, directory);
    dialog.show(selectedTransaction);
  }
}
