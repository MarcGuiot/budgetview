package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.*;

import java.util.Set;

public class UpdateAccountOnTransactionDelete extends AbstractChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    Set<Key> deletedTransaction = changeSet.getDeleted(Transaction.TYPE);
    if (deletedTransaction.isEmpty()){
      return;
    }
    GlobList accounts = repository.getAll(Account.TYPE);
    for (Glob account : accounts) {
      if (account.get(Account.LAST_TRANSACTION) != null && deletedTransaction.contains(Key.create(Transaction.TYPE, account.get(Account.LAST_TRANSACTION)))) {
        repository.update(account.getKey(), Account.LAST_TRANSACTION, null);
      }
    }
  }
}
