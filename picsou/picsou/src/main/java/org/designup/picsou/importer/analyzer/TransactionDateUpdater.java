package org.designup.picsou.importer.analyzer;

import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.designup.picsou.model.Transaction;

import java.text.SimpleDateFormat;

public class TransactionDateUpdater implements TransactionTypeFinalizer {
  public boolean processTransaction(Glob transaction, GlobRepository repository, SimpleDateFormat format) {
    if ((transaction.get(Transaction.MONTH) == null) || (transaction.get(Transaction.DAY) == null)) {
      repository.update(transaction.getKey(),
                        value(Transaction.MONTH, transaction.get(Transaction.BANK_MONTH)),
                        value(Transaction.DAY, transaction.get(Transaction.BANK_DAY)));
      return true;
    }
    return false;
  }
}
