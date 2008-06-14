package org.designup.picsou.importer.analyzer;

import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import java.text.SimpleDateFormat;

public class LabelForCategorizationUpdater implements TransactionTypeFinalizer {
  public boolean processTransaction(Glob transaction, GlobRepository globRepository, SimpleDateFormat format) {
    String label = transaction.get(Transaction.LABEL);
    String note = transaction.get(Transaction.NOTE);
    globRepository.update(transaction.getKey(), Transaction.LABEL_FOR_CATEGORISATION,
                          AllocationLearningService.anonymise(note, label, transaction.get(Transaction.TRANSACTION_TYPE)));
    return false;
  }
}
