package com.budgetview.io.importer.json;

import com.budgetview.gui.components.dialogs.PicsouDialog;
import com.budgetview.io.importer.AccountFileImporter;
import com.budgetview.io.importer.utils.ImportedTransactionIdGenerator;
import com.budgetview.model.ImportType;
import com.budgetview.model.ImportedTransaction;
import com.budgetview.model.RealAccount;
import com.budgetview.shared.utils.Amounts;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.OperationCancelled;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;

import static org.globsframework.model.FieldValue.value;

public class
JsonImporter implements AccountFileImporter {

  public GlobList loadTransactions(Reader reader, GlobRepository initialRepository, GlobRepository targetRepository, PicsouDialog current) throws InvalidFormat, OperationCancelled, IOException {

    ImportedTransactionIdGenerator generator = new ImportedTransactionIdGenerator(targetRepository.getIdGenerator());
    JSONObject jsonArray = new JSONObject(Files.loadStreamToString(reader));

    GlobList createdTransactions = new GlobList();
    for (Object item : jsonArray.getJSONArray("transactions")) {
      JSONObject jsonTransaction = (JSONObject) item;

      Glob importedTransaction = parseTransaction(jsonTransaction, targetRepository, generator);
      if (importedTransaction != null) {
        createdTransactions.add(importedTransaction);
      }
    }

    return createdTransactions;
  }

  private Glob parseTransaction(JSONObject jsonTransaction,
                                GlobRepository targetRepository,
                                ImportedTransactionIdGenerator generator)
    throws InvalidFormat, OperationCancelled, IOException {

    if (isTrue(jsonTransaction.get("deleted"))) {
      return null;
    }

    Date date = parseDate(jsonTransaction.get("date"));
    Date bankDate = parseDate(jsonTransaction.get("rdate"));
    double amount = parseAmount(jsonTransaction.get("value"));
    String originalLabel = parseString(jsonTransaction.get("original_wording"));
    String simplifiedLabel = parseString(jsonTransaction.get("wording"));
    String budgeaAccountId = parseString(jsonTransaction.get("id_account"));
    int realAccountId = getAccountId(budgeaAccountId, targetRepository);

    Glob importedTransaction =
      targetRepository.create(ImportedTransaction.TYPE,
                              value(ImportedTransaction.ID, generator.getNextId(ImportedTransaction.ID, 1)),
                              value(ImportedTransaction.ACCOUNT, realAccountId),
                              value(ImportedTransaction.DATE, Dates.toString(date)),
                              value(ImportedTransaction.BANK_DATE, Dates.toString(bankDate)),
                              value(ImportedTransaction.AMOUNT, amount),
                              value(ImportedTransaction.OFX_NAME, simplifiedLabel),
                              value(ImportedTransaction.IMPORT_TYPE, ImportType.JSON.getId()));

    return importedTransaction;
  }

  private boolean isTrue(Object deleted) {
    return "true".equalsIgnoreCase(deleted.toString());
  }

  private int getAccountId(String budgeaAccountId, GlobRepository targetRepository) {
    GlobList index = targetRepository.findByIndex(RealAccount.BUDGEA_ID_INDEX, budgeaAccountId);
    if (index.size() == 0) {
      throw new OperationCancelled("Cannot find account from API with id: " + budgeaAccountId);
    }
    Glob realAccount = index.getFirst();
    return realAccount.get(RealAccount.ID);
  }

  private String parseString(Object value) {
    return Strings.toString(value);
  }

  private Date parseDate(Object value) {
    checkNotNull(value);
    return JsonUtils.parseDate(value.toString());
  }

  private int parseInt(Object value) {
    checkNotNull(value);
    try {
      return Integer.parseInt(value.toString());
    }
    catch (NumberFormatException e) {
      throw new InvalidFormat(e);
    }
  }

  private double parseAmount(Object value) {
    checkNotNull(value);
    return Amounts.extractAmount(value.toString());
  }

  private void checkNotNull(Object value) {
    if (value == null) {
      throw new InvalidFormat("Unexpected null value");
    }
  }
}
