package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountUpdateMode;
import org.designup.picsou.model.ReconciliationStatus;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobPrinter;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class ReconciliationDetectionTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Glob transaction = repository.get(key);
        if (Transaction.isManuallyCreated(transaction)) {
          Glob account = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
          if (AccountUpdateMode.isAutomatic(account)) {
            setToReconcile(key, repository);
          }
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });

    changeSet.safeVisit(Account.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        updateAccountTransactions(key, repository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        updateAccountTransactions(key, repository);
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  private void updateAccountTransactions(Key accountKey, GlobRepository repository) {
    Glob account = repository.get(accountKey);
    if (Account.isManualUpdateAccount(account)) {
      return;
    }
    GlobList transactions = repository.getAll(Transaction.TYPE, linkedTo(accountKey, Transaction.ACCOUNT));
    for (Glob transaction : transactions) {
      if (ReconciliationStatus.canBeSet(transaction) && Transaction.isManuallyCreated(transaction)) {
        setToReconcile(transaction.getKey(), repository);
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {

  }

  private void setToReconcile(Key key, GlobRepository repository) {
    repository.update(key, Transaction.RECONCILIATION_STATUS,
                      ReconciliationStatus.TO_RECONCILE.getId());
  }
}
