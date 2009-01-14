package org.designup.picsou.importer.qif;

import org.designup.picsou.importer.utils.ImportedTransactionIdGenerator;
import org.designup.picsou.model.ImportedTransaction;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.FieldValuesBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobIdGenerator;
import org.globsframework.utils.Strings;

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
      String mValue = null;
      String nValue = null;
      String pValue = null;
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
            values.set(ImportedTransaction.AMOUNT, extractAmount(reader.readLine()));
            break;
          case 'P':
            pValue = reader.readLine();
            break;
          case 'M':
            mValue = reader.readLine();
            break;
          case 'N':
            nValue = reader.readLine();
            break;
          case '^':
            String value = null;
            if (!Strings.isNullOrEmpty(mValue)) {
              value = mValue;
            }
            else if (!Strings.isNullOrEmpty(pValue)) {
              value = pValue;
            }
            if (!Strings.isNullOrEmpty(mValue) && !Strings.isNullOrEmpty(pValue)) {
              if (mValue.startsWith(pValue.substring(0, pValue.length() > 10 ? 10 : pValue.length()))) {
                value = mValue;
              }
              else {
                value = Strings.join(pValue, mValue);
              }
            }
            values.set(ImportedTransaction.BANK_TRANSACTION_TYPE, nValue != null ? nValue.trim() : null);
            values.set(ImportedTransaction.ORIGINAL_LABEL, value != null ? value.trim() : null);
            values.set(ImportedTransaction.LABEL, value != null ? value.trim() : null);
            return createTransaction(values);
        }
      }
    }
    catch (IOException e) {
      return null;
    }
  }

  private double extractAmount(final String amount) throws IOException {
    String tmp = amount.replaceAll(",", "").replaceAll("\\.", "");
    if (Strings.isNullOrEmpty(tmp)) {
      return 0.;
    }
    return Double.parseDouble(tmp) / 100.0;
  }

  private Glob createTransaction(FieldValuesBuilder values) {
    return globRepository.create(ImportedTransaction.TYPE, values.get().toArray());
  }

  private void readDate(FieldValuesBuilder values) throws IOException {
    String value = reader.readLine().trim();
    values.set(ImportedTransaction.BANK_DATE, value);
  }
}
