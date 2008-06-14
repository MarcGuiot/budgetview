package org.designup.picsou.importer.analyzer;

import org.designup.picsou.model.Transaction;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

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
