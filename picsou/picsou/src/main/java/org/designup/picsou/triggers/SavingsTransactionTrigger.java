package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;

public class SavingsTransactionTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {

        Integer oppositeTransaction = values.get(Transaction.SAVINGS_TRANSACTION);
        Integer seriesId = values.get(Transaction.SERIES);
        if (seriesId != null && oppositeTransaction == null) {
          createSavingsTransactionIfSavingsSeries(key, values, seriesId, repository);
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob transaction = repository.get(key);
        Integer savingsTransactionId = transaction.get(Transaction.SAVINGS_TRANSACTION);
        if (values.contains(Transaction.SERIES)) {
          Glob previousSeries = repository.find(Key.create(Series.TYPE,
                                                           values.getPrevious(Transaction.SERIES)));
          if (previousSeries != null) {
            if (savingsTransactionId != null) {
              repository.delete(Key.create(Transaction.TYPE, savingsTransactionId));
            }
          }
          Integer newSeriesId = values.get(Transaction.SERIES);
          if (newSeriesId != null) {
            savingsTransactionId =
              createSavingsTransactionIfSavingsSeries(key, transaction, newSeriesId, repository);
          }
        }
        if (values.contains(Transaction.AMOUNT) && savingsTransactionId != null) {
          repository.update(Key.create(Transaction.TYPE, savingsTransactionId),
                            Transaction.AMOUNT, -values.get(Transaction.AMOUNT));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer savingsTransaction = previousValues.get(Transaction.SAVINGS_TRANSACTION);
        if (savingsTransaction != null) {
          repository.delete(Key.create(Transaction.TYPE, savingsTransaction));
        }
      }
    });
  }

  private Integer createSavingsTransactionIfSavingsSeries(Key key, FieldValues values, Integer seriesId,
                                                          GlobRepository repository) {
    Glob series = repository.find(Key.create(Series.TYPE, seriesId));
    Glob savingsAccount = repository.findLinkTarget(series, Series.SAVINGS_ACCOUNT);
    if (savingsAccount != null && !savingsAccount.get(Account.IS_IMPORTED_ACCOUNT)) {
      Double amount = -values.get(Transaction.AMOUNT);
      Glob savingsTransaction =
        repository.create(Transaction.TYPE,
                          FieldValue.value(Transaction.AMOUNT, amount),
                          FieldValue.value(Transaction.ACCOUNT, savingsAccount.get(Account.ID)),
                          FieldValue.value(Transaction.BANK_DAY, values.get(Transaction.BANK_DAY)),
                          FieldValue.value(Transaction.BANK_MONTH, values.get(Transaction.BANK_MONTH)),
                          FieldValue.value(Transaction.DAY, values.get(Transaction.DAY)),
                          FieldValue.value(Transaction.MONTH, values.get(Transaction.MONTH)),
                          FieldValue.value(Transaction.TRANSACTION_TYPE,
                                           amount > 0 ? TransactionType.VIREMENT.getId() : TransactionType.PRELEVEMENT.getId()),
                          FieldValue.value(Transaction.CATEGORY,
                                           values.get(Transaction.CATEGORY)),
                          FieldValue.value(Transaction.LABEL, values.get(Transaction.LABEL)),
                          FieldValue.value(Transaction.SERIES, values.get(Transaction.SERIES)),
                          FieldValue.value(Transaction.MIRROR, true),
                          FieldValue.value(Transaction.PLANNED,
                                           values.get(Transaction.PLANNED)));
      repository.update(key, Transaction.SAVINGS_TRANSACTION,
                        savingsTransaction.get(Transaction.ID));
      return savingsTransaction.get(Transaction.ID);
    }
    return null;
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
