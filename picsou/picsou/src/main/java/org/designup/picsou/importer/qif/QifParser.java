package org.designup.picsou.importer.qif;

import org.designup.picsou.importer.utils.ImportedTransactionIdGenerator;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobIdGenerator;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class QifParser {

  public static GlobList read(Reader reader, ReadOnlyGlobRepository initialRepository, GlobRepository globRepository) {
    QifParser qifParser = new QifParser(reader, initialRepository, globRepository);
    return qifParser.run();
  }

  private BufferedReader reader;
  private ReadOnlyGlobRepository initialRepository;
  private GlobRepository globRepository;
  private GlobIdGenerator globIdGenerator;
  private Integer accountId = null;
  private Integer subSeriesId;
  private Integer seriesId;
  GlobList result = new GlobList();
  private String date;

  String mValue = null;
  String nValue = null;
  String pValue = null;

  private QifParser(Reader reader, ReadOnlyGlobRepository initialRepository, GlobRepository globRepository) {
    this.initialRepository = initialRepository;
    this.globRepository = globRepository;
    this.reader = new BufferedReader(reader);
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
              if (updated) {
                Glob transaction = createTransaction(values);
                result.add(transaction);
              }
              return null;
            }
            case '!':
              reader.readLine();
              break;
            case 'D':
              updated = true;
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
              values.set(ImportedTransaction.SPLIT, Boolean.TRUE);
              completeDefault(values);
              Glob transaction = createTransaction(values);
              seriesId = null;
              subSeriesId = null;
              result.add(transaction);
              return new SplitedNextTransactionReader(transaction.get(ImportedTransaction.ID), this);
            }
            case '^':
              completeDefault(values);
              Glob transaction = createTransaction(values);
              result.add(transaction);
              reader.readLine();
              seriesId = null;
              subSeriesId = null;
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
      values.set(ImportedTransaction.IS_OFX, false);
      values.set(ImportedTransaction.ACCOUNT, accountId);
      values.set(ImportedTransaction.SERIES, seriesId);
      values.set(ImportedTransaction.SUB_SERIES, subSeriesId);
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
                subSeriesId = null;
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
                subSeriesId = null;
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
//        values.set(ImportedTransaction.SPLIT, Boolean.TRUE);
        values.set(ImportedTransaction.SPLIT_SOURCE, parentId);
      }
    }
  }

  private void addEnveloppe(String line) {
    String[] tmp = line.split(":");
    String seriesName = tmp[0];
    Glob series = globRepository.getAll(Series.TYPE, GlobMatchers.fieldEquals(Series.NAME, seriesName)).getFirst();
    if (series == null) {
      series = initialRepository.getAll(Series.TYPE, GlobMatchers.fieldEquals(Series.NAME, seriesName)).getFirst();
    }
    boolean forceCreation = false;
    if (series == null) {
      series = globRepository.create(Series.TYPE, FieldValue.value(Series.NAME, seriesName));
      forceCreation = true;
    }
    Glob subSeries = null;
    if (tmp.length > 1) {
      String subSeriesName = tmp[1];
      if (forceCreation) {
        subSeries = globRepository.create(SubSeries.TYPE, FieldValue.value(SubSeries.SERIES, series.get(Series.ID)),
                                          FieldValue.value(SubSeries.NAME, subSeriesName));
      }
      else {
        GlobMatcher seriesFilter = GlobMatchers.fieldEquals(SubSeries.SERIES, series.get(Series.ID));
        subSeries = initialRepository.getAll(SubSeries.TYPE,
                                             GlobMatchers.and(GlobMatchers.fieldEquals(SubSeries.NAME, subSeriesName),
                                                              seriesFilter)).getFirst();

        if (subSeries == null) {
          subSeries = globRepository.getAll(SubSeries.TYPE,
                                            GlobMatchers.and(GlobMatchers.fieldEquals(SubSeries.NAME, subSeriesName),
                                                             seriesFilter)).getFirst();
        }
        if (subSeries == null) {
          subSeries = globRepository.create(SubSeries.TYPE, FieldValue.value(SubSeries.SERIES, series.get(Series.ID)),
                                            FieldValue.value(SubSeries.NAME, subSeriesName));
        }
      }
    }
    subSeriesId = subSeries != null ? subSeries.get(SubSeries.ID) : null;
    seriesId = series.get(Series.ID);
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
