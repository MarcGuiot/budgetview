package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;

public class SavingsSeriesTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (!changeSet.containsChanges(Series.TYPE)) {
      return;
    }
    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {

      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (!values.contains(Series.SAVINGS_ACCOUNT)) {
          return;
        }
        Integer previousAccountId = values.getPrevious(Series.SAVINGS_ACCOUNT);
        Integer newAccountId = values.get(Series.SAVINGS_ACCOUNT);
        if (previousAccountId != null) {
          Glob previousAccount = repository.get(Key.create(Account.TYPE, previousAccountId));
          if (previousAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.SAVINGS.getId())) {
            GlobList transactions =
              repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, key.get(Series.ID)).getGlobs();
            for (Glob transaction : transactions) {
              if (Transaction.isMirrorTransaction(transaction)) {
                repository.delete(transaction.getKey());
              }
            }
          }
        }
        if (newAccountId != null) {
          Glob newAccount = repository.get(Key.create(Account.TYPE, newAccountId));
          GlobList transactions =
            repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, key.get(Series.ID)).getGlobs();
          for (Glob transaction : transactions) {
            SavingsTransactionTrigger.createSavingsTransactionIfSavingsSeries(transaction.getKey(), transaction,
                                                                              newAccount, repository);
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }


  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
