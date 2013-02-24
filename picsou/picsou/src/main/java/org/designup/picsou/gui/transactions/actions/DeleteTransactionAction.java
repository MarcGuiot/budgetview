package org.designup.picsou.gui.transactions.actions;

import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.MessageType;
import org.designup.picsou.gui.transactions.edition.DeleteTransactionDialog;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.actions.MultiSelectionAction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Iterator;

public class DeleteTransactionAction extends MultiSelectionAction {

  public DeleteTransactionAction(GlobRepository repository, Directory directory) {
    super(Lang.get("transaction.delete.action"), Transaction.TYPE, repository, directory);
  }

  protected void process(GlobList transactions, GlobRepository repository, Directory directory) {
    if (hasPlanned(transactions)) {
      MessageDialog.show("transaction.delete.title.forbidden", MessageType.ERROR, directory,
                         "transaction.delete.planned");
      return;
    }

    if (hasAutoCreated(transactions)) {
      MessageDialog.show("transaction.delete.title.forbidden", MessageType.ERROR, directory,
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
