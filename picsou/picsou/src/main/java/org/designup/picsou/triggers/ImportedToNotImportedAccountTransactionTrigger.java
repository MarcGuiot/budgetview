package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
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
        if (seriesId != null && oppositeTransaction == null
            && !values.isTrue(Transaction.MIRROR)
            && !values.isTrue(Transaction.PLANNED)) {
          Glob series = repository.get(Key.create(Series.TYPE, seriesId));
          if (series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId())) {
            Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
            Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
            if (Account.shouldCreateMirrorTransaction(fromAccount, toAccount)) {
              Integer targetAccount = repository.findLinkTarget(series, Series.MIRROR_SERIES).get(Series.TARGET_ACCOUNT);
              TransactionUtils.createMirrorTransaction(key, values, targetAccount,
                                                       series.get(Series.MIRROR_SERIES), repository);
            }
          }
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob transaction = repository.find(key);
        if (transaction == null || transaction.isTrue(Transaction.MIRROR) || transaction.isTrue(Transaction.PLANNED)) {
          return;
        }
        Integer mirrorTransactionId = transaction.get(Transaction.NOT_IMPORTED_TRANSACTION);
        if (values.contains(Transaction.SERIES)) {
          if (mirrorTransactionId != null) {
            Key mirrorKey = Key.create(Transaction.TYPE, mirrorTransactionId);
            //destruction de series
            Glob glob = repository.find(mirrorKey);
            if (glob != null) {
              repository.delete(mirrorKey);
            }
          }
          Integer newSeriesId = values.get(Transaction.SERIES);
          if (newSeriesId != null) {
            Glob series = repository.find(Key.create(Series.TYPE, newSeriesId));
            Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
            Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
            if (Account.shouldCreateMirrorTransaction(fromAccount, toAccount)) {
              Integer targetAccount = repository.findLinkTarget(series, Series.MIRROR_SERIES).get(Series.TARGET_ACCOUNT);
              TransactionUtils.createMirrorTransaction(key, transaction, targetAccount,
                                                       series.get(Series.MIRROR_SERIES), repository);
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
            if (values.contains(Transaction.BUDGET_DAY)) {
              repository.update(mirrorKey, Transaction.BUDGET_DAY, values.get(Transaction.BUDGET_DAY));
            }
            if (values.contains(Transaction.BUDGET_MONTH)) {
              repository.update(mirrorKey, Transaction.BUDGET_MONTH, values.get(Transaction.BUDGET_MONTH));
            }
            if (values.contains(Transaction.POSITION_DAY)) {
              repository.update(mirrorKey, Transaction.POSITION_DAY, values.get(Transaction.POSITION_DAY));
            }
            if (values.contains(Transaction.POSITION_MONTH)) {
              repository.update(mirrorKey, Transaction.POSITION_MONTH, values.get(Transaction.POSITION_MONTH));
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

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
