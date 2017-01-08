package com.budgetview.io.importer.json;

import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.description.Labels;
import com.budgetview.desktop.series.utils.SeriesMatchers;
import com.budgetview.io.importer.AccountFileImporter;
import com.budgetview.io.importer.utils.ImportedTransactionIdGenerator;
import com.budgetview.model.*;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.shared.model.DefaultSeries;
import org.globsframework.json.JsonGlobFormat;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Files;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.OperationCancelled;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEqualsIgnoreCase;

public class JsonImporter implements AccountFileImporter {

  public GlobList loadTransactions(Reader reader, GlobRepository initialRepository, GlobRepository targetRepository, PicsouDialog current) throws InvalidFormat, OperationCancelled, IOException {

    ImportedTransactionIdGenerator generator = new ImportedTransactionIdGenerator(targetRepository.getIdGenerator());
    JSONObject jsonAccount = new JSONObject(Files.loadStreamToString(reader));

    //---------------
    System.out.println("JsonImporter.loadTransactions: parsing \n" + jsonAccount.toString(2));
    //---------------

    Glob realAccount = getRealAccount(jsonAccount, initialRepository);
    if (realAccount == null) {
      throw new OperationCancelled("Cannot find real account, should have been created in first phase. Content:\n" + jsonAccount.toString(2));
    }

    GlobList createdTransactions = new GlobList();
    for (Object item : jsonAccount.getJSONArray("transactions")) {
      JSONObject jsonTransaction = (JSONObject) item;

      Glob importedTransaction = parseTransaction(jsonTransaction, realAccount, targetRepository, generator);
      if (importedTransaction != null) {
        createdTransactions.add(importedTransaction);
      }
    }


    Glob lastImportedTransaction = createdTransactions.sort(ImportedTransaction.BANK_DATE).getLast();
    if (lastImportedTransaction != null) {
      targetRepository.update(realAccount, RealAccount.TRANSACTION_ID, lastImportedTransaction.get(ImportedTransaction.ID));
    }
    else {
      targetRepository.update(realAccount,
                              value(RealAccount.TRANSACTION_ID, null),
                              value(RealAccount.POSITION, Double.toString(jsonAccount.getDouble("position"))),
                              value(RealAccount.POSITION_DATE, Month.toDate(jsonAccount.getInt("position_month"), jsonAccount.getInt("position_day"))),
                              value(RealAccount.FILE_CONTENT, "{}"));
    }

    return createdTransactions;
  }

  private Glob parseTransaction(JSONObject jsonTransaction,
                                Glob realAccount,
                                GlobRepository targetRepository,
                                ImportedTransactionIdGenerator generator)
    throws InvalidFormat, OperationCancelled, IOException {

    if (isTrue(jsonTransaction.get("deleted"))) {
      return null;
    }

    String bankDate = convertDate(jsonTransaction.getString("bank_date"));

    return
      targetRepository.create(ImportedTransaction.TYPE,
                              value(ImportedTransaction.ID, generator.getNextId(ImportedTransaction.ID, 1)),
                              value(ImportedTransaction.ACCOUNT, realAccount.get(RealAccount.ID)),
                              value(ImportedTransaction.DATE, convertDate(jsonTransaction.getString("operation_date"))),
                              value(ImportedTransaction.BANK_DATE, bankDate),
                              value(ImportedTransaction.AMOUNT, jsonTransaction.getDouble("amount")),
                              value(ImportedTransaction.SIMPLE_LABEL, jsonTransaction.getString("label")),
                              value(ImportedTransaction.OFX_NAME, jsonTransaction.getString("original_label")),
                              value(ImportedTransaction.SERIES, findOrCreateSeriesId(jsonTransaction.optInt("default_series_id"),
                                                                                     bankDate,
                                                                                     targetRepository)),
                              value(ImportedTransaction.IMPORT_TYPE, ImportType.JSON.getId()));
  }

  private Integer findOrCreateSeriesId(Integer defaultSeriesId, String bankDate, GlobRepository targetRepository) {
    DefaultSeries defaultSeries = DefaultSeries.find(defaultSeriesId);
    if (DefaultSeries.UNCATEGORIZED.equals(defaultSeries)) {
      defaultSeries = null;
    }

    String defaultSeriesLabel = Labels.get(defaultSeries);
    GlobList importedSeriesList = new GlobList();
    if (defaultSeries != null) {
      importedSeriesList = targetRepository.getAll(ImportedSeries.TYPE, fieldEqualsIgnoreCase(ImportedSeries.NAME, defaultSeriesLabel));
    }
    Glob series = null;
    if (importedSeriesList.isEmpty()) {
      GlobList seriesList =
        targetRepository.getAll(Series.TYPE,
                                and(fieldEqualsIgnoreCase(Series.NAME, defaultSeriesLabel),
                                    SeriesMatchers.activeInMonth(Month.getMonthId(Dates.parse(bankDate)))));
      if (!seriesList.isEmpty()) {
        series = seriesList.getFirst();
      }
    }

    String name;
    BudgetArea budgetArea;
    if (series != null) {
      name = series.get(Series.NAME);
      budgetArea = Series.getBudgetArea(series);
    }
    else if (!importedSeriesList.isEmpty()) {
      name = defaultSeries.getName();
      budgetArea = defaultSeries.getBudgetArea();
    }
    else {
      return null;
    }

    if (importedSeriesList.isEmpty()) {
      Glob created = targetRepository.create(ImportedSeries.TYPE,
                                             value(ImportedSeries.NAME, name),
                                             value(ImportedSeries.SERIES, series != null ? series.get(Series.ID) : null),
                                             value(ImportedSeries.BUDGET_AREA, budgetArea.getId()));
      GlobPrinter.print(created);
      importedSeriesList = new GlobList(created);
    }
    return importedSeriesList.getFirst().get(ImportedSeries.ID);
  }

  private boolean isTrue(Object deleted) {
    return "true".equalsIgnoreCase(deleted.toString());
  }

  private Glob getRealAccount(JSONObject jsonAccount, GlobRepository repository) {
    return RealAccount.findFromProvider(jsonAccount.getInt("provider"),
                                        jsonAccount.getInt("provider_account_id"),
                                        repository);
  }

  private String convertDate(String date) {
    checkNotNull(date);
    try {
      return Dates.toString(JsonGlobFormat.parseDate(date));
    }
    catch (ParseException e) {
      throw new InvalidFormat("Cannot parse date: " + date, e);
    }
  }

  private void checkNotNull(Object value) {
    if (value == null) {
      throw new InvalidFormat("Unexpected null value");
    }
  }
}
