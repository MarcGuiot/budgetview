package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

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
        Glob transaction = repository.find(key);
        if (transaction == null || transaction.get(Transaction.MIRROR)) {
          return;
        }
        Integer mirrorTransactionId = transaction.get(Transaction.NOT_IMPORTED_TRANSACTION);
        if (values.contains(Transaction.SERIES)) {
          if (mirrorTransactionId != null) {
            repository.delete(Key.create(Transaction.TYPE, mirrorTransactionId));
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
        else {
          if (mirrorTransactionId != null) {
            Key mirorKey = Key.create(Transaction.TYPE, mirrorTransactionId);
            if (values.contains(Transaction.AMOUNT) && mirrorTransactionId != null) {
              repository.update(mirorKey,
                                Transaction.AMOUNT, -values.get(Transaction.AMOUNT));
            }
            if (values.contains(Transaction.DAY)) {
              repository.update(mirorKey, Transaction.DAY, values.get(Transaction.DAY));
            }
            if (values.contains(Transaction.MONTH)) {
              repository.update(mirorKey, Transaction.MONTH, values.get(Transaction.MONTH));
            }
            if (values.contains(Transaction.LABEL)) {
              repository.update(mirorKey, Transaction.LABEL, values.get(Transaction.LABEL));
            }
            if (values.contains(Transaction.CATEGORY)) {
              repository.update(mirorKey, Transaction.CATEGORY, values.get(Transaction.CATEGORY));
            }
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer savingsTransaction = previousValues.get(Transaction.NOT_IMPORTED_TRANSACTION);
        // la transaction peut avoir ete detruite par si la seriesbudget a été aussi detruite cf trigger SeriesBudgetUpdateTransactionTrigger
        if (savingsTransaction != null && repository.find(Key.create(Transaction.TYPE, savingsTransaction)) != null) {
          repository.delete(Key.create(Transaction.TYPE, savingsTransaction));
        }
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
