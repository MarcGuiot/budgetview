package com.budgetview.io.importer.qif;

import com.budgetview.io.importer.utils.ImportedTransactionIdGenerator;
import com.budgetview.io.importer.utils.TypedInputStream;
import com.budgetview.model.ImportType;
import com.budgetview.model.ImportedSeries;
import com.budgetview.model.ImportedTransaction;
import com.budgetview.model.RealAccount;
import com.budgetview.shared.utils.Amounts;
import org.globsframework.model.*;
import org.globsframework.model.repository.GlobIdGenerator;
import org.globsframework.utils.Strings;

import java.io.BufferedReader;
import java.io.IOException;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class QifParser {

  public static GlobList read(TypedInputStream inputStream, ReadOnlyGlobRepository initialRepository, GlobRepository globRepository) {
    QifParser qifParser = new QifParser(inputStream, initialRepository, globRepository);
    return qifParser.run();
  }

  private final BufferedReader reader;
  private final GlobRepository globRepository;
  private final GlobIdGenerator globIdGenerator;
  private Integer accountId = null;
  private Integer seriesId;
  GlobList result = new GlobList();
  private String date;

  String mValue = null;
  String nValue = null;
  String pValue = null;

  private QifParser(TypedInputStream inputStream, ReadOnlyGlobRepository initialRepository, GlobRepository globRepository) {
    this.globRepository = globRepository;
    this.reader = new BufferedReader(inputStream.getBestProbableReader());
    globIdGenerator = new ImportedTransactionIdGenerator(globRepository.getIdGenerator());
  }

  private GlobList run() {
    NextTransactionReader reader = new DefaultReader();
    while (reader != null) {
      reader = reader.readNext();
    }
    return result;
  }

  interface NextTransactionReader {
    NextTransactionReader readNext();
  }

  class DefaultReader implements NextTransactionReader {

    public NextTransactionReader readNext() {
      try {
        FieldValuesBuilder values =
          FieldValuesBuilder.init()
            .set(ImportedTransaction.ID, globIdGenerator.getNextId(ImportedTransaction.ID, 1));
        boolean updated = false;
        while (true) {
          int start = reader.read();
          switch (start) {
            case -1: {
              if (updated && date != null) {
                Glob transaction = createTransaction(values);
                result.add(transaction);
              }
              return null;
            }
            case '!':
              reader.readLine();
              break;
            case 'D':
              date = readDate(values);
              break;
            case 'T':
              updated = true;
              values.set(ImportedTransaction.AMOUNT, Amounts.extractAmount(reader.readLine()));
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
            case 'L': {
              String line = reader.readLine();
              addEnveloppe(line);
              break;
            }
            case 'S': {
              if (updated && date != null) {
                values.set(ImportedTransaction.SPLIT, Boolean.TRUE);
                completeDefault(values);
                Glob transaction = createTransaction(values);
                seriesId = null;
                result.add(transaction);
                return new SplitedNextTransactionReader(transaction.get(ImportedTransaction.ID), this);
              }
              else {
                reader.readLine();
              }
              return this;
            }
            case '^':
              if (updated && date != null) {
                completeDefault(values);
                Glob transaction = createTransaction(values);
                result.add(transaction);
              }
              reader.readLine();
              seriesId = null;
              date = null;
              mValue = null;
              pValue = null;
              nValue = null;
              return this;
            case '\n':
              break;
            default:
              reader.readLine();
          }
        }
      }
      catch (IOException e) {
        return null;
      }
    }

    private void completeDefault(FieldValuesBuilder values) {
      if (accountId == null) {
        accountId = globRepository.create(RealAccount.TYPE).get(RealAccount.ID);
      }
      if (Strings.isNotEmpty(mValue)) {
        values.set(ImportedTransaction.QIF_M, mValue);
      }
      if (Strings.isNotEmpty(pValue)) {
        values.set(ImportedTransaction.QIF_P, pValue);
      }
      values.set(ImportedTransaction.BANK_TRANSACTION_TYPE, nValue != null ? nValue.trim() : null);
      values.set(ImportedTransaction.IMPORT_TYPE, ImportType.QIF.getId());
      values.set(ImportedTransaction.ACCOUNT, accountId);
      values.set(ImportedTransaction.SERIES, seriesId);
    }

    private class SplitedNextTransactionReader implements NextTransactionReader {
      private Integer parentId;
      private NextTransactionReader normal;

      public SplitedNextTransactionReader(Integer parentId, NextTransactionReader normal) {
        this.parentId = parentId;
        this.normal = normal;
      }

      public NextTransactionReader readNext() {
        try {
          FieldValuesBuilder values =
            FieldValuesBuilder.init()
              .set(ImportedTransaction.ID, globIdGenerator.getNextId(ImportedTransaction.ID, 1));
          boolean updated = false;

          String enveloppe = reader.readLine();
          if (Strings.isNotEmpty(enveloppe)) {
            addEnveloppe(enveloppe);
          }
          values.set(ImportedTransaction.BANK_DATE, date);

          while (true) {
            int start = reader.read();
            switch (start) {
              case -1: {
                if (updated) {
                  Glob transaction = createTransaction(values);
                  result.add(transaction);
                }
                return this;
              }
              case 'E':
                values.set(ImportedTransaction.NOTE, reader.readLine());
                break;
              case '$':
                double amount = Amounts.extractAmount(reader.readLine());
                values.set(ImportedTransaction.AMOUNT, amount);
                Glob glob = globRepository.get(Key.create(ImportedTransaction.TYPE, parentId));
                globRepository.update(glob.getKey(), ImportedTransaction.AMOUNT,
                                      glob.get(ImportedTransaction.AMOUNT) - amount);
                completeValues(values);
                seriesId = null;
                Glob transaction = createTransaction(values);
                result.add(transaction);
                return new NextTransactionReader() {
                  public NextTransactionReader readNext() {
                    try {
                      int i = reader.read();
                      switch (i) {
                        case -1:
                          return null;
                        case '^':
                          reader.readLine();
                          return normal;
                        case 'S':
                          return SplitedNextTransactionReader.this;
                      }
                    }
                    catch (IOException e) {
                      return null;
                    }
                    return null;
                  }
                };
              case '^':
                seriesId = null;
                date = null;
                reader.readLine();
                return normal;
              case '\n':
                break;
              default:
                reader.readLine();
            }
          }
        }
        catch (IOException e) {
          return null;
        }
      }

      private void completeValues(FieldValuesBuilder values) {
        completeDefault(values);
        values.set(ImportedTransaction.SPLIT_SOURCE, parentId);
      }
    }
  }

  private void addEnveloppe(String line) {
    String seriesName = line.trim();
    Glob series = globRepository.getAll(ImportedSeries.TYPE, fieldEquals(ImportedSeries.NAME, seriesName)).getFirst();
    if (series == null) {
      series = globRepository.create(ImportedSeries.TYPE, value(ImportedSeries.NAME, seriesName));
    }
    seriesId = series.get(ImportedSeries.ID);
  }

  private Glob createTransaction(FieldValuesBuilder values) {
    return globRepository.create(ImportedTransaction.TYPE, values.get().toArray());
  }

  private String readDate(FieldValuesBuilder values) throws IOException {
    String value = reader.readLine().trim();
    String date = value.replaceAll("[^0-9]+", "/");
    values.set(ImportedTransaction.BANK_DATE, date);
    return date;
  }
}
