package com.budgetview.functests.utils;

import com.budgetview.functests.checkers.ImportDialogChecker;
import com.budgetview.functests.checkers.ImportDialogPreviewChecker;
import com.budgetview.functests.checkers.OperationChecker;
import com.budgetview.io.exporter.ofx.OfxExporter;
import com.budgetview.model.*;
import junit.framework.TestCase;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.Key;
import org.globsframework.utils.Dates;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.exceptions.ResourceAccessFailed;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import static com.budgetview.model.Transaction.*;
import static org.globsframework.model.FieldValue.value;

public class OfxBuilder {

  public static final String DEFAULT_ACCOUNT_ID = "00001123";
  public static final String DEFAULT_ACCOUNT_NAME = "Account n. 00001123";

  private GlobRepository repository;
  private String fileName;
  private OperationChecker operations;
  private Glob currentAccount;

  public static OfxBuilder init(LoggedInFunctionalTestCase testCase) {
    return new OfxBuilder(getFileName(testCase), testCase.getOperations());
  }

  public static OfxBuilder init(TestCase testCase) {
    return new OfxBuilder(TestUtils.getFileName(testCase, ".ofx"), null);
  }

  public static OfxBuilder init(String fileName) {
    return new OfxBuilder(fileName, null);
  }

  public static OfxBuilder init(TestCase testCase, OperationChecker operations) {
    return new OfxBuilder(TestUtils.getFileName(testCase, ".ofx"), operations);
  }

  private OfxBuilder(String fileName, OperationChecker operations) {
    this.fileName = fileName;
    this.operations = operations;
    this.repository =
      GlobRepositoryBuilder.init()
        .add(TransactionType.values())
        .get();
    repository.create(Key.create(Bank.TYPE, Bank.GENERIC_BANK_ID));
    repository.create(Key.create(BankEntity.TYPE, BankEntity.GENERIC_BANK_ENTITY_ID));
  }

  public OfxBuilder addBankAccount(String accountNumber, double position, String updateDate) {
    return addBankAccount("-1", 1234, accountNumber, position, updateDate);
  }

  public OfxBuilder addBankAccount(int bankEntityId, int branchId, String accountNumber, double position, String updateDate) {
    return addBankAccount(Integer.toString(bankEntityId), branchId, accountNumber, position, updateDate);
  }

  public OfxBuilder addBankAccount(String bankEntity, int branchId, String accountNumber, double position, String updateDate) {
    Integer bankEntityId = BankEntity.find(bankEntity, repository);
    Integer bankId = Bank.GENERIC_BANK_ID;
    if (bankEntityId != null) {
      bankId = BankEntity.getBank(repository.find(Key.create(BankEntity.TYPE, bankEntityId)), repository)
        .get(Bank.ID);
    }
    currentAccount =
      repository.create(Key.create(Account.TYPE, repository.getIdGenerator().getNextId(Account.ID, 1)),
                        value(Account.BANK, bankId),
                        value(Account.BANK_ENTITY, bankEntityId),
                        value(Account.BANK_ENTITY_LABEL, bankEntity),
                        value(Account.BRANCH_ID, branchId),
                        value(Account.NUMBER, accountNumber),
                        value(Account.POSITION_WITH_PENDING, position),
                        value(Account.POSITION_DATE, updateDate != null ? Dates.parse(updateDate) : null));
    return this;
  }

  public OfxBuilder addCardAccount(String cardId, double position, String updateDate) {
    String bankEntityLabel = "-1";
    Integer bankEntityId = BankEntity.find(bankEntityLabel, repository);
    Integer bankId = Bank.GENERIC_BANK_ID;
    currentAccount =
      repository.create(Key.create(Account.TYPE, repository.getIdGenerator().getNextId(Account.ID, 1)),
                        value(Account.NUMBER, cardId),
                        value(Account.BANK, bankId),
                        value(Account.BANK_ENTITY, bankEntityId),
                        value(Account.BANK_ENTITY_LABEL, bankEntityLabel),
                        value(Account.POSITION_WITH_PENDING, position),
                        value(Account.POSITION_DATE, updateDate != null ? Dates.parse(updateDate) : null),
                        value(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()));
    return this;
  }

  public OfxBuilder addTransaction(String yyyyMMdd, double amount, String label) {
    return doAddTransaction(yyyyMMdd, null, amount, label, null, null);
  }

  public OfxBuilder addTransaction(String userDate, String bankDate, double amount, String label) {
    return doAddTransaction(userDate, bankDate, amount, label, null, null);
  }

  public OfxBuilder addTransactionWithNote(String yyyyMMdd, double amount, String label, String note) {
    return doAddTransaction(yyyyMMdd, null, amount, label, note, null);
  }

  private OfxBuilder doAddTransaction(String userDate, String bankDate, double amount, String label, String note,
                                      Integer parentId) {
    if (currentAccount == null) {
      addBankAccount(30066, 1234, DEFAULT_ACCOUNT_ID, 0.0, null);
    }
    if (bankDate == null) {
      bankDate = userDate;
    }
    Date parsedUserDate = Dates.parse(userDate);
    Date parsedBankDate = Dates.parse(bankDate);
    Glob transaction =
      repository.create(TYPE,
                        value(AMOUNT, amount),
                        value(MONTH, Month.getMonthId(parsedUserDate)),
                        value(DAY, Month.getDay(parsedUserDate)),
                        value(BANK_MONTH, Month.getMonthId(parsedBankDate)),
                        value(BANK_DAY, Month.getDay(parsedBankDate)),
                        value(LABEL, label),
                        value(ORIGINAL_LABEL, label),
                        value(ACCOUNT, currentAccount.get(Account.ID)),
                        value(ORIGINAL_ACCOUNT, currentAccount.get(Account.ID)),
                        value(NOTE, note));
    if (parentId != null) {
      repository.update(transaction.getKey(), Transaction.SPLIT_SOURCE, parentId);
    }

    return this;
  }

  public String save() {
    writeFile(fileName);
    return fileName;
  }

  public void load() {
    save();
    operations.importOfxFile(fileName);
  }

  public void loadInAccount(String name) {
    save();
    operations.openImportDialog()
      .setFilePath(fileName)
      .importFileAndPreview()
      .selectAccount(name)
      .importAccountAndComplete();
  }

  public void load(String newAccount, String existingAccount) {
    save();
    operations.importOfxOnAccount(fileName, existingAccount);
  }

  public void loadInNewAccount() {
    save();
    ImportDialogPreviewChecker preview = operations.openImportDialog()
      .setFilePath(fileName)
      .importFileAndPreview();
    if (preview.accountIsEditable()) {
      preview.setMainAccount();
    }
    preview.importAccountAndComplete();
  }

  public void load(int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransactionCount) {
    save();
    ImportDialogPreviewChecker preview = operations.openImportDialog()
      .setFilePath(fileName)
      .importFileAndPreview();
    if (preview.accountIsEditable()) {
      preview.setMainAccount();
    }
    preview
      .importAccountAndGetSummary()
      .checkSummaryAndValidate(importedTransactionCount, ignoredTransactionCount, autocategorizedTransactionCount);
  }

  public void loadAndGotoCategorize(int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransactionCount) {
    save();
    operations.openImportDialog()
      .setFilePath(fileName)
      .importFileAndPreview()
      .importAccountAndGetSummary()
      .checkSummaryAndValidate(importedTransactionCount, ignoredTransactionCount, autocategorizedTransactionCount);
  }

  public void loadDeferredCard(String accountName, String targetAccountName) {
    save();
    importDefered(accountName, fileName, true, targetAccountName);
  }

  public void loadOneDeferredCard(String bank, String targetAccountName) {
    save();
    operations.openImportDialog()
      .setFilePath(fileName)
      .importFileAndPreview()
      .setDeferredAccount(25, 28, 0, targetAccountName)
      .selectBank(bank)
      .importAccountAndComplete();
  }

  private void importDefered(String accountName, final String fileName,
                             boolean withMainAccount, String targetAccountName) {
    ImportDialogChecker importDialog = operations.openImportDialog();
    importDialog.importDeferred(accountName, fileName, withMainAccount, targetAccountName);
  }

  public void loadUnknown(String bank) {
    save();
    operations.importOfxFile(fileName, bank);
  }

  private void writeFile(String name) {
    FileWriter writer = null;
    try {
      File file = new File(name);
      file.getParentFile().mkdirs();
      writer = new FileWriter(file);
      OfxExporter.write(repository, writer, true);
    }
    catch (IOException e) {
      throw new ResourceAccessFailed(e);
    }
    finally {
      if (writer != null) {
        try {
          writer.close();
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public static String getFileName(TestCase testCase) {
    return TestUtils.getFileName(testCase, ".ofx");
  }

}
