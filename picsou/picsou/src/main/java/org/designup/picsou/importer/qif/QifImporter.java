package org.designup.picsou.importer.qif;

import org.crossbowlabs.globs.model.FieldValuesBuilder;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.designup.picsou.importer.AccountFileImporter;
import org.designup.picsou.importer.analyzer.TransactionAnalyzer;
import org.designup.picsou.model.Bank;

import java.io.Reader;

public class QifImporter implements AccountFileImporter {
  public static final int DEFAULT_BANK_ID = 30003;

  private TransactionAnalyzer transactionAnalyzer;

  public QifImporter(TransactionAnalyzer analyzer) {
    this.transactionAnalyzer = analyzer;
  }

  public GlobList loadTransactions(Reader reader, GlobRepository globRepository) {
    globRepository.findOrCreate(Key.create(Bank.TYPE, DEFAULT_BANK_ID));
    GlobList transactions = QifParser.read(reader, globRepository);
    transactionAnalyzer.processTransactions(transactions, globRepository);
    return transactions;
  }
}
