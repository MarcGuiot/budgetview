package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.components.dialogs.ConfirmationDialog;
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

    editorPane.setText(getText(transactions,
                               hasSplit(transactions),
                               hasAutoCreated(transactions),
                               hasPlanned(transactions)));
    
    if (transactions.isEmpty()) {
      cancel.setEnabled(false);
    }
  }

  private String getText(GlobList transactions,
                         boolean hasSplit,
                         boolean hasAutoCreated,
                         boolean hasPlanned) {
    String text = "<html>";
    if (hasSplit) {
      text = Lang.get("transaction.delete.split");
    }
    if (hasAutoCreated) {
      text += Lang.get("transaction.delete.savings");
    }
    if (hasPlanned) {
      text += Lang.get("transaction.delete.planned");
    }
    if (!transactions.isEmpty()) {
      if (transactions.size() == 1) {
        text += Lang.get("transaction.delete.default.single");
      }
      else {
        text += Lang.get("transaction.delete.default.multi", transactions.size());
      }
    }
    text += "</html>";
    return text;
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

  private boolean hasSplit(GlobList transactions) {
    boolean hasSplit = false;
    for (Iterator it = transactions.iterator(); it.hasNext(); ) {
      Glob glob = (Glob)it.next();
      if (glob.get(Transaction.SPLIT_SOURCE) != null) {
        it.remove();
        hasSplit = true;
      }
    }
    return hasSplit;
  }

  protected void postValidate() {
    try {
      repository.startChangeSet();
      while (!transactions.isEmpty()) {
        Glob glob = transactions.remove(0);
        if (Transaction.isSplit(glob)) {
          GlobList linkedToList = repository.findLinkedTo(glob, Transaction.SPLIT_SOURCE);
          repository.delete(linkedToList);
          for (Glob transaction : linkedToList) {
            transactions.remove(transaction);
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
