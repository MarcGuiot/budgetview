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
            Integer accountID = getAccount(fromAccount, toAccount);
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
            Key mirrorKey = Key.create(Transaction.TYPE, mirrorTransactionId);
            //destruction de series
            Glob glob = repository.find(mirrorKey);
            if (glob != null){
              repository.delete(mirrorKey);
            }
          }
          Integer newSeriesId = values.get(Transaction.SERIES);
          if (newSeriesId != null) {
            Glob series = repository.find(Key.create(Series.TYPE, newSeriesId));
            Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
            Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
            if (Account.shoudCreateMirror(fromAccount, toAccount)) {
              Integer accountID = getAccount(fromAccount, toAccount);
              TransactionUtils.createMirrorTransaction(key, transaction, accountID, repository);
            }
          }
        }
        else {
          if (mirrorTransactionId != null) {
            Key mirrorKey = Key.create(Transaction.TYPE, mirrorTransactionId);
            if (values.contains(Transaction.AMOUNT) && mirrorTransactionId != null) {
              repository.update(mirrorKey,
                                Transaction.AMOUNT, -values.get(Transaction.AMOUNT));
            }
            if (values.contains(Transaction.DAY)) {
              repository.update(mirrorKey, Transaction.DAY, values.get(Transaction.DAY));
            }
            if (values.contains(Transaction.MONTH)) {
              repository.update(mirrorKey, Transaction.MONTH, values.get(Transaction.MONTH));
            }
            if (values.contains(Transaction.LABEL)) {
              repository.update(mirrorKey, Transaction.LABEL, values.get(Transaction.LABEL));
            }
            if (values.contains(Transaction.CATEGORY)) {
              repository.update(mirrorKey, Transaction.CATEGORY, values.get(Transaction.CATEGORY));
            }
            if (values.contains(Transaction.SUB_SERIES)) {
              repository.update(mirrorKey, Transaction.SUB_SERIES, values.get(Transaction.SUB_SERIES));
            }
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer savingsTransaction = previousValues.get(Transaction.NOT_IMPORTED_TRANSACTION);
        // la transaction peut avoir ete detruite par si la seriesbudget a été aussi detruite cf trigger SeriesBudgetUpdateTransactionTrigger
        if ((savingsTransaction != null) && repository.contains(Key.create(Transaction.TYPE, savingsTransaction))) {
          repository.delete(Key.create(Transaction.TYPE, savingsTransaction));
        }
      }
    });
  }

  private Integer getAccount(Glob fromAccount, Glob toAccount) {
    if (fromAccount.get(Account.IS_IMPORTED_ACCOUNT)){
      return toAccount.get(Account.ID);
    }
    return fromAccount.get(Account.ID);
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
