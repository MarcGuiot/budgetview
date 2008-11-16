package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.Set;

public class SavingsSeriesCreationTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Transaction.TYPE, new DefaultChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Transaction.SERIES)) {
          Glob series = repository.findLinkTarget(repository.get(key), Transaction.SERIES);
          if (series != null && series.get(Series.SAVINGS_ACCOUNT) == null && series.get(Series.SAVINGS_SERIES) != null) {
            Glob savingsSeries = repository.findLinkTarget(series, Series.SAVINGS_SERIES);
            Glob account = repository.findLinkTarget(savingsSeries, Series.SAVINGS_ACCOUNT);
            if (!account.get(Account.IS_IMPORTED_ACCOUNT)) {
              Glob transaction = repository.get(key);
              Integer pendingTransaction = transaction.get(Transaction.SAVINGS_TRANSACTION);
              if (pendingTransaction != null) {
                repository.delete(Key.create(Transaction.TYPE, pendingTransaction));
              }
              FieldValuesBuilder valuesBuilder = FieldValuesBuilder.init();
              valuesBuilder.set(Transaction.ACCOUNT, account.get(Account.ID))
                .set(Transaction.AMOUNT, -transaction.get(Transaction.AMOUNT))
                .set(Transaction.BANK_DAY, transaction.get(Transaction.BANK_DAY))
                .set(Transaction.BANK_MONTH, transaction.get(Transaction.BANK_MONTH))
                .set(Transaction.CATEGORY, transaction.get(Transaction.CATEGORY))
                .set(Transaction.DAY, transaction.get(Transaction.DAY))
                .set(Transaction.MONTH, transaction.get(Transaction.MONTH))
                .set(Transaction.LABEL, transaction.get(Transaction.LABEL))
                .set(Transaction.LABEL_FOR_CATEGORISATION, transaction.get(Transaction.LABEL_FOR_CATEGORISATION))
                .set(Transaction.SAVINGS_TRANSACTION, transaction.get(Transaction.ID))
                .set(Transaction.SERIES, savingsSeries.get(Series.ID))
                .set(Transaction.TRANSACTION_TYPE, TransactionType.VIREMENT.getId())
                ;
              repository.create(Transaction.TYPE, valuesBuilder.toArray());
            }
          }
        }
      }

      public void visitDeletion(Key key, FieldValues values) throws Exception {
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
