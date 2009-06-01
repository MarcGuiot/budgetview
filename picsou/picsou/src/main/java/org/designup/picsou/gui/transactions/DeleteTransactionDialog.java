package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.components.ConfirmationDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.awt.*;
import java.util.Iterator;

public class DeleteTransactionDialog extends ConfirmationDialog {
  private GlobList transactions;
  private GlobRepository repository;

  public DeleteTransactionDialog(GlobList transactions, Window parent,
                                 GlobRepository repository, Directory directory) {
    super("transaction.delete.title", "transaction.delete.content", parent, directory);
    this.transactions = transactions;
    this.repository = repository;
    boolean hasSplit = false;
    for (Iterator it = transactions.iterator(); it.hasNext();) {
      Glob glob = (Glob)it.next();
      if (glob.get(Transaction.SPLIT_SOURCE) != null) {
        it.remove();
        hasSplit = true;
      }
    }
    boolean hasAutoCreated = false;
    for (Iterator<Glob> iterator = transactions.iterator(); iterator.hasNext();) {
      Glob glob = iterator.next();
      if (Transaction.isCreatedBySeries(glob) || Transaction.isMirrorTransaction(glob)) {
        iterator.remove();
        hasAutoCreated = true;
      }
    }
    boolean hasPlanned = false;
    for (Iterator<Glob> iterator = transactions.iterator(); iterator.hasNext();) {
      Glob glob = iterator.next();
      if (Transaction.isPlanned(glob)) {
        iterator.remove();
        hasPlanned = true;
      }
    }
    String text = "<html>";
    if (hasSplit) {
      text = Lang.get("transaction.delete.split");
    }
    if (hasAutoCreated) {
      text = text + Lang.get("transaction.delete.savings");
    }
    if (hasPlanned) {
      text = text + Lang.get("transaction.delete.planned");
    }
    text += "</html>";
    editorPane.setText(text);
    if (transactions.isEmpty()) {
      cancel.setEnabled(false);
    }
  }

  protected void postValidate() {
    try {
      repository.startChangeSet();
      while (!transactions.isEmpty()) {
        Glob glob = transactions.remove(0);
        GlobList linkedTo = null;
        if (Transaction.isSplit(glob)) {
          linkedTo = repository.findLinkedTo(glob, Transaction.SPLIT_SOURCE);
          repository.delete(linkedTo);
          for (Glob glob1 : linkedTo) {
            transactions.remove(glob1);
          }
        }
        Glob account = repository.findLinkTarget(glob, Transaction.ACCOUNT);
        if (account != null && glob.get(Transaction.ID).equals(account.get(Account.TRANSACTION_ID))) {
          repository.update(account.getKey(), Account.TRANSACTION_ID, null);
        }
        repository.delete(glob.getKey());
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

}
