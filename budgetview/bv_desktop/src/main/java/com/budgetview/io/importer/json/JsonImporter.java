package com.budgetview.io.importer.json;

import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.description.Labels;
import com.budgetview.io.importer.AccountFileImporter;
import com.budgetview.io.importer.utils.ImportedTransactionIdGenerator;
import com.budgetview.model.ImportType;
import com.budgetview.model.ImportedSeries;
import com.budgetview.model.ImportedTransaction;
import com.budgetview.model.RealAccount;
import com.budgetview.shared.cloud.budgea.BudgeaSeriesConverter;
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
    System.out.println("JsonImporter.loadTransactions: looking for \n" + jsonAccount.toString(2));
    System.out.println("JsonImporter.loadTransactions: INITIAL");
    GlobPrinter.print(initialRepository, RealAccount.TYPE);
    System.out.println("JsonImporter.loadTransactions: TARGET");
    GlobPrinter.print(targetRepository, RealAccount.TYPE);
    //---------------


    Glob realAccount = getRealAccount(jsonAccount, initialRepository);
    System.out.println("JsonImporter.loadTransactions ==> " + realAccount);
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

    System.out.println("JsonImporter.loadTransactions - created:");
    GlobPrinter.print(createdTransactions);

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

    String originalLabel = jsonTransaction.getString("original_label");

    String bankDate = convertDate(jsonTransaction.getString("bank_date"));
    Glob importedTransaction =
      targetRepository.create(ImportedTransaction.TYPE,
                              value(ImportedTransaction.ID, generator.getNextId(ImportedTransaction.ID, 1)),
                              value(ImportedTransaction.ACCOUNT, realAccount.get(RealAccount.ID)),
                              value(ImportedTransaction.DATE, convertDate(jsonTransaction.getString("operation_date"))),
                              value(ImportedTransaction.BANK_DATE, bankDate),
                              value(ImportedTransaction.AMOUNT, jsonTransaction.getDouble("amount")),
                              value(ImportedTransaction.OFX_NAME, jsonTransaction.getString("label")),
                              value(ImportedTransaction.SERIES, findOrCreateSeriesId(jsonTransaction.optInt("provider_category_id"),
                                                                                     jsonTransaction.optString("provider_category_name"),
                                                                                     bankDate,
                                                                                     targetRepository)),
                              value(ImportedTransaction.IMPORT_TYPE, ImportType.JSON.getId()));

    return importedTransaction;
  }

  private Integer findOrCreateSeriesId(Integer providerSeriesId, String providerSeriesName, String bankDate, GlobRepository targetRepository) {
    BudgeaSeriesConverter converter = new BudgeaSeriesConverter();
    DefaultSeries defaultSeries = converter.convert(providerSeriesId);
    return findOrCreateSeriesId(defaultSeries, providerSeriesName, bankDate, targetRepository);
  }

  private Integer findOrCreateSeriesId(DefaultSeries defaultSeries, String providerSeriesName, String bankDate, GlobRepository targetRepository) {
    System.out.println("JsonImporter.findOrCreateSeriesId for " + providerSeriesName);
    GlobList importedSeriesList = new GlobList();
    if (defaultSeries != null) {
      importedSeriesList = targetRepository.getAll(ImportedSeries.TYPE, and(fieldEqualsIgnoreCase(ImportedSeries.NAME, Labels.get(defaultSeries))));
    }
    BudgetArea budgetArea;
    if (!importedSeriesList.isEmpty()) {
      budgetArea = defaultSeries.getBudgetArea();
    }
    else {
      System.out.println("   ==> no series found for defaultSeries, try with provider name");
      importedSeriesList = targetRepository.getAll(ImportedSeries.TYPE, and(fieldEqualsIgnoreCase(ImportedSeries.NAME, providerSeriesName)));
      budgetArea = BudgetArea.VARIABLE;
    }
    if (importedSeriesList.isEmpty()) {
      System.out.println("   ==> create from scratch");
      importedSeriesList = new GlobList(
        targetRepository.create(ImportedSeries.TYPE,
                                value(ImportedSeries.NAME, providerSeriesName),
                                value(ImportedSeries.BUDGET_AREA, budgetArea.getId())));
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
