package com.budgetview.io.importer.analyzer;

import com.budgetview.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public class LabelForCategorizationUpdater implements TransactionTypeFinalizer {
  public boolean processTransaction(Glob transaction, GlobRepository globRepository) {
    String label = transaction.get(Transaction.LABEL);
    String note = transaction.get(Transaction.NOTE);
    String anonymizedLabel = Transaction.anonymise(note, label, transaction.get(Transaction.TRANSACTION_TYPE));
    globRepository.update(transaction.getKey(), Transaction.LABEL_FOR_CATEGORISATION, anonymizedLabel);
    return false;
  }
}
