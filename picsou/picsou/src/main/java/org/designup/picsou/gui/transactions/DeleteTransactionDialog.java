package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.components.dialogs.ConfirmationDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class DeleteTransactionDialog extends ConfirmationDialog {
  private GlobList transactions;
  private GlobRepository repository;

  public DeleteTransactionDialog(GlobList transactions,
                                 GlobRepository repository,
                                 Directory directory) {
    super("transaction.delete.title", Lang.get(getContentKey(transactions)), directory);
    this.transactions = transactions;
    this.repository = repository;
  }

  private static String getContentKey(GlobList transactions) {
    int totalCount = transactions.size();
    int splitCount = getSplitCount(transactions);
    if (splitCount == 0) {
      if (transactions.size() == 1) {
        return "transaction.delete.default.single";
      }
      else {
        return "transaction.delete.default.multi";
      }
    }
    else if (splitCount < totalCount) {
      return "transaction.delete.split.mixed";
    }
    else {
      if (transactions.size() == 1) {
        return "transaction.delete.split.single";
      }
      else {
        return "transaction.delete.split.multi";
      }
    }

  }

  private static int getSplitCount(GlobList transactions) {
    int result = 0;
    for (Glob transaction : transactions) {
      if (transaction.get(Transaction.SPLIT_SOURCE) != null) {
        result++;
      }
    }
    return result;
  }

  protected void postValidate() {
    try {
      repository.startChangeSet();
      while (!transactions.isEmpty()) {
        Glob toDelete = transactions.remove(0);

        if (Transaction.isSplitSource(toDelete)) {
          GlobList parts = repository.findLinkedTo(toDelete, Transaction.SPLIT_SOURCE);
          repository.delete(parts);
          transactions.removeAll(parts);
        }

        if (Transaction.isSplitPart(toDelete)) {
          Glob source = repository.findLinkTarget(toDelete, Transaction.SPLIT_SOURCE);
          repository.update(source.getKey(), Transaction.AMOUNT,
                            source.get(Transaction.AMOUNT) + toDelete.get(Transaction.AMOUNT));
        }

        Glob account = repository.findLinkTarget(toDelete, Transaction.ACCOUNT);
        if (account != null && toDelete.get(Transaction.ID).equals(account.get(Account.TRANSACTION_ID))) {
          repository.update(account.getKey(), Account.TRANSACTION_ID, null);
        }
        repository.delete(toDelete.getKey());
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

}
