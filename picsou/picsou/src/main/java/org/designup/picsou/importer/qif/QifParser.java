package org.designup.picsou.importer.qif;

import org.crossbowlabs.globs.model.FieldValuesBuilder;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.utils.CachedGlobIdGenerator;
import org.crossbowlabs.globs.model.utils.GlobIdGenerator;
import org.crossbowlabs.globs.utils.exceptions.InvalidData;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QifParser {
  public static final SimpleDateFormat QIF_DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");
  public static final SimpleDateFormat QIF_DATE_LONG_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

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
    globIdGenerator = new CachedGlobIdGenerator(globRepository.getIdGenerator());
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
          .set(Transaction.ID, globIdGenerator.getNextId(Transaction.ID, 1));
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
    String value = reader.readLine().trim();
    try {
      Date date;
      if (value.length() == 8) {
        date = QIF_DATE_FORMAT.parse(value);
      }
      else {
        date = QIF_DATE_LONG_FORMAT.parse(value);
      }
      values.set(Transaction.BANK_MONTH, Month.get(date));
      values.set(Transaction.BANK_DAY, Month.getDay(date));
    }
    catch (ParseException e) {
      throw new InvalidData("Unable to parse date " + value + " in format " + "dd/mm/yy", e);
    }
  }
}
