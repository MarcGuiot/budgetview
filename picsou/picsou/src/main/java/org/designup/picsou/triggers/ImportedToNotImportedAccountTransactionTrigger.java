package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Set;

public class ImportedToNotImportedAccountTransactionTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {

        Integer oppositeTransaction = values.get(Transaction.NOT_IMPORTED_TRANSACTION);
        Integer seriesId = values.get(Transaction.SERIES);
        if (seriesId != null && oppositeTransaction == null && !values.get(Transaction.MIRROR)) {
          Glob series = repository.find(Key.create(Series.TYPE, seriesId));
          Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
          Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
          if (Account.shoudCreateMirror(fromAccount, toAccount)) {
            Integer accountID = getAccount(values, fromAccount, toAccount);
            TransactionUtils.createMirrorTransaction(key, values, accountID, repository);
          }
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob transaction = repository.get(key);
        Integer createdTransactionId = transaction.get(Transaction.NOT_IMPORTED_TRANSACTION);
        if (values.contains(Transaction.SERIES)) {
          Glob previousSeries = repository.find(Key.create(Series.TYPE,
                                                           values.getPrevious(Transaction.SERIES)));
          if (previousSeries != null) {
            if (createdTransactionId != null) {
              repository.delete(Key.create(Transaction.TYPE, createdTransactionId));
            }
          }
          Integer newSeriesId = values.get(Transaction.SERIES);
          if (newSeriesId != null) {
            Glob series = repository.find(Key.create(Series.TYPE, newSeriesId));
            Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
            Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
            if (Account.shoudCreateMirror(fromAccount, toAccount)) {
              Integer accountID = getAccount(transaction, fromAccount, toAccount);
              TransactionUtils.createMirrorTransaction(key, transaction, accountID, repository);
            }
          }
        }
        if (values.contains(Transaction.AMOUNT) && createdTransactionId != null) {
          repository.update(Key.create(Transaction.TYPE, createdTransactionId),
                            Transaction.AMOUNT, -values.get(Transaction.AMOUNT));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer savingsTransaction = previousValues.get(Transaction.NOT_IMPORTED_TRANSACTION);
        if (savingsTransaction != null) {
          repository.delete(Key.create(Transaction.TYPE, savingsTransaction));
        }
      }
    });

    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Series.FROM_ACCOUNT) || values.contains(Series.TO_ACCOUNT)) {
          GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, key.get(Series.ID))
            .getGlobs().filterSelf(GlobMatchers.fieldEquals(Transaction.MIRROR, false), repository);
          for (Glob transaction : transactions) {
            repository.update(transaction.getKey(), Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID);
            repository.update(transaction.getKey(), Transaction.CATEGORY, Category.NONE);
            Integer opposateTransaction = transaction.get(Transaction.NOT_IMPORTED_TRANSACTION);
            if (opposateTransaction != null) {
              repository.delete(Key.create(Transaction.TYPE, opposateTransaction));
              repository.update(transaction.getKey(), Transaction.NOT_IMPORTED_TRANSACTION, null);
            }
            if (transaction.get(Transaction.PLANNED)) {
              repository.delete(transaction.getKey());
            }
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  private Integer getAccount(FieldValues values, Glob fromAccount, Glob toAccount) {
    Integer accountID;
    if (fromAccount.get(Account.ID) == Account.MAIN_SUMMARY_ACCOUNT_ID) {
      return toAccount.get(Account.ID);
    }
    if (toAccount.get(Account.ID) == Account.MAIN_SUMMARY_ACCOUNT_ID) {
      return fromAccount.get(Account.ID);
    }
    if (values.get(Transaction.ACCOUNT).equals(fromAccount.get(Series.FROM_ACCOUNT))) {
      accountID = fromAccount.get(Series.TO_ACCOUNT);
    }
    else {
      accountID = fromAccount.get(Series.FROM_ACCOUNT);
    }
    return accountID;
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
