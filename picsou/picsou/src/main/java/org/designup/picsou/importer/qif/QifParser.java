package org.designup.picsou.importer.qif;

import org.designup.picsou.importer.utils.ImportedTransactionIdGenerator;
import org.designup.picsou.model.ImportedTransaction;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.FieldValuesBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobIdGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class QifParser {

  public static GlobList read(Reader reader, GlobRepository globRepository) {
    QifParser qifParser = new QifParser(reader, globRepository);
    return qifParser.run();
  }

  private BufferedReader reader;
  private GlobRepository globRepository;
  private GlobIdGenerator globIdGenerator;

  private QifParser(Reader reader, GlobRepository globRepository) {
    this.globRepository = globRepository;
    this.reader = new BufferedReader(reader);
    globIdGenerator = new ImportedTransactionIdGenerator(globRepository.getIdGenerator());
  }

  private GlobList run() {
    GlobList result = new GlobList();
    for (Glob glob = readNextTransaction(); glob != null; glob = readNextTransaction()) {
      result.add(glob);
    }
    return result;
  }

  private Glob readNextTransaction() {
    try {
      FieldValuesBuilder values =
        FieldValuesBuilder.init()
          .set(Transaction.ID, globIdGenerator.getNextId(ImportedTransaction.ID, 1));
      boolean updated = false;
      StringBuilder description = new StringBuilder();
      while (true) {
        int start = reader.read();
        switch (start) {
          case -1:
            return updated ? createTransaction(values) : null;
          case '!':
            reader.readLine();
            break;
          case 'D':
            updated = true;
            readDate(values);
            break;
          case 'T':
            updated = true;
            values.set(ImportedTransaction.AMOUNT, Double.parseDouble(reader.readLine().replaceAll(",", "")));
            break;
          case 'P':
            reader.readLine();
            break;
          case 'M':
            append(description);
            break;
          case 'N':
            append(description);
            break;
          case '^':
            String value = description.toString();
            values.set(ImportedTransaction.ORIGINAL_LABEL, value);
            values.set(ImportedTransaction.LABEL, value.trim());
            return createTransaction(values);
        }
      }
    }
    catch (IOException e) {
      return null;
    }
  }

  private void append(StringBuilder description) throws IOException {
    if (description.length() > 0) {
      description.append(" ");
    }
    description.append(reader.readLine());
  }

  private Glob createTransaction(FieldValuesBuilder values) {
    return globRepository.create(ImportedTransaction.TYPE, values.get().toArray());
  }

  private void readDate(FieldValuesBuilder values) throws IOException {
    String value = reader.readLine().trim();
    values.set(ImportedTransaction.BANK_DATE, value);
  }
}
