package org.designup.picsou.importer;

import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;
import org.crossbowlabs.globs.utils.exceptions.TruncatedFile;
import org.designup.picsou.importer.ofx.OfxImporter;
import org.designup.picsou.importer.qif.QifImporter;
import org.designup.picsou.importer.utils.TypedInputStream;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionImport;

import java.io.IOException;
import java.util.Date;

public class ImportService {

  public Key run(TypedInputStream fileStream, ReadOnlyGlobRepository initialRepository,
                 GlobRepository targetRepository) throws IOException, ItemNotFound, TruncatedFile {
    AccountFileImporter importer = getImporter(fileStream.getType());
    GlobList createdTransactions =
      importer.loadTransactions(fileStream.getBestProbableReader(), initialRepository, targetRepository);
    return createImport(fileStream, createdTransactions, targetRepository);
  }

  private Key createImport(TypedInputStream file, GlobList createdTransactions, GlobRepository targetRepository) {
    Glob transactionImport =
      targetRepository.create(TransactionImport.TYPE,
                              FieldValue.value(TransactionImport.IMPORT_DATE, new Date()),
                              FieldValue.value(TransactionImport.SOURCE, file.getName()));

    Key importKey = transactionImport.getKey();

    int lastMonth = 0;
    int lastDay = 0;
    for (Glob createdTransaction : createdTransactions) {
      targetRepository.setTarget(createdTransaction.getKey(), Transaction.IMPORT, importKey);

      Integer transactionMonth = createdTransaction.get(Transaction.BANK_MONTH);
      Integer transactionDay = createdTransaction.get(Transaction.BANK_DAY);
      if (lastMonth < transactionMonth || (lastMonth == transactionMonth && lastDay < transactionDay)) {
        lastMonth = transactionMonth;
        lastDay = transactionDay;
      }
    }

    targetRepository.update(importKey,
                            TransactionImport.LAST_TRANSACTION_DATE,
                            Month.toDate(lastMonth, lastDay));
    return importKey;
  }

  private AccountFileImporter getImporter(BankFileType type) throws ItemNotFound {
    if (type == BankFileType.OFX) {
      return new TransactionFilter(new OfxImporter());
    }
    if (type == BankFileType.QIF) {
      return new TransactionFilter(new QifImporter());
    }
    throw new ItemNotFound("Unknown file extension for " + type);
  }
}
