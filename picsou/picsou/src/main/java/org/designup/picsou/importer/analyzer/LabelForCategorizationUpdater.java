package org.designup.picsou.importer.analyzer;

import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public class LabelForCategorizationUpdater implements TransactionTypeFinalizer {
  public boolean processTransaction(Glob transaction, GlobRepository globRepository) {
    String label = transaction.get(Transaction.LABEL);
    String note = transaction.get(Transaction.NOTE);
    globRepository.update(transaction.getKey(), Transaction.LABEL_FOR_CATEGORISATION,
                          Transaction.anonymise(note, label, transaction.get(Transaction.TRANSACTION_TYPE)));
    return false;
  }
}
