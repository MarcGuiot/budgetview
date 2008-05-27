package org.designup.picsou.importer.qif;

import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.utils.exceptions.InvalidData;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QifParser {
  public static final SimpleDateFormat QIF_DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");
  private static NumberFormat AMOUNT_FORMAT = DecimalFormat.getNumberInstance(Locale.FRANCE);
  private static final String DEFAULT_ACCOUNT_ID = "0";
  private static final String DEFAULT_ACCOUNT_NAME = "Compte principal";

  public static GlobList read(Reader reader, GlobRepository globRepository) {
    QifParser qifParser = new QifParser(reader, globRepository);
    return qifParser.run();
  }

  private Integer defaultAccountId;
  private BufferedReader reader;
  private GlobRepository globRepository;

  private QifParser(Reader reader, GlobRepository globRepository) {
    this.globRepository = globRepository;
    this.reader = new BufferedReader(reader);
    defaultAccountId = createDefaultAccountIfNeeded(globRepository).get(Account.ID);
  }

  public static Glob createDefaultAccountIfNeeded(GlobRepository globRepository) {
    Glob glob = globRepository.findUnique(Account.TYPE, value(Account.NUMBER, DEFAULT_ACCOUNT_ID));
    if (glob == null) {
      glob = globRepository.create(Key.create(Account.TYPE, globRepository.getIdGenerator().getNextId(Account.ID, 1)),
                                   value(Account.NUMBER, DEFAULT_ACCOUNT_ID),
                                   value(Account.NAME, DEFAULT_ACCOUNT_NAME),
                                   value(Account.BANK, QifImporter.DEFAULT_BANK_ID));
    }
    return glob;
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
          .set(Transaction.ID, globRepository.getNextId(Transaction.ID, 1))
          .set(Transaction.ACCOUNT, defaultAccountId);
      boolean updated = false;
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
            values.set(Transaction.AMOUNT, Double.parseDouble(reader.readLine().replaceAll(",", "")));
            break;
          case 'P':
            updated = true;
            updateDescription(values, reader.readLine());
            break;
          case 'M':
            updated = true;
            updateDescription(values, reader.readLine());
            break;
          case 'N':
            reader.readLine();
            break;
          case '^':
            return createTransaction(values);
        }
      }
    }
    catch (IOException e) {
      return null;
    }
  }

  private Glob createTransaction(FieldValuesBuilder values) {
    return globRepository.create(Transaction.TYPE, values.get().toArray());
  }

  private void updateDescription(FieldValuesBuilder values, String value) {
    String description = values.get().get(Transaction.ORIGINAL_LABEL);
    if (description == null || description.length() < value.length()) {
      values.set(Transaction.ORIGINAL_LABEL, value);
      values.set(Transaction.LABEL, value);
    }
  }

  private void readDate(FieldValuesBuilder values) throws IOException {
    String value = reader.readLine();
    try {
      Date date = QIF_DATE_FORMAT.parse(value);
      values.set(Transaction.MONTH, Month.get(date));
      values.set(Transaction.DAY, Month.getDay(date));
    }
    catch (ParseException e) {
      throw new InvalidData("Unable to parse date " + value + " in format " + "dd/mm/yy", e);
    }
  }
}
