package com.budgetview.importer.analyzer;

import com.budgetview.model.Transaction;

import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public class TransactionDateUpdater implements TransactionTypeFinalizer {
  public boolean processTransaction(Glob transaction, GlobRepository repository) {
    if ((transaction.get(Transaction.MONTH) == null) || (transaction.get(Transaction.DAY) == null)) {
      repository.update(transaction.getKey(),
                        value(Transaction.MONTH, transaction.get(Transaction.BANK_MONTH)),
                        value(Transaction.DAY, transaction.get(Transaction.BANK_DAY)));
    }
    if ((transaction.get(Transaction.BUDGET_MONTH) == null) || (transaction.get(Transaction.BUDGET_DAY) == null)) {
      repository.update(transaction.getKey(),
                        value(Transaction.BUDGET_MONTH, transaction.get(Transaction.MONTH)),
                        value(Transaction.BUDGET_DAY, transaction.get(Transaction.DAY)));
    }
    if ((transaction.get(Transaction.POSITION_MONTH) == null) || (transaction.get(Transaction.POSITION_DAY) == null)) {
      repository.update(transaction.getKey(),
                        value(Transaction.POSITION_MONTH, transaction.get(Transaction.BANK_MONTH)),
                        value(Transaction.POSITION_DAY, transaction.get(Transaction.BANK_DAY)));
    }
    return false;
  }
}
