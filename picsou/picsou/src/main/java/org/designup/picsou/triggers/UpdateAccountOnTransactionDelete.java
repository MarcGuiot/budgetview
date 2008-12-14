package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;

public class UpdateAccountOnTransactionDelete implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    Set<Key> deletedTransaction = changeSet.getDeleted(Transaction.TYPE);
    GlobList accounts = repository.getAll(Account.TYPE);
    for (Glob account : accounts) {
      if (account.get(Account.TRANSACTION_ID) != null && deletedTransaction.contains(Key.create(Transaction.TYPE, account.get(Account.TRANSACTION_ID)))) {
        repository.update(account.getKey(), Account.TRANSACTION_ID, null);
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
