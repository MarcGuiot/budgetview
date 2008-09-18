package org.designup.picsou.functests.utils;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.designup.picsou.functests.checkers.OperationChecker;
import org.designup.picsou.importer.ofx.OfxExporter;
import org.designup.picsou.model.*;
import static org.designup.picsou.model.Transaction.*;
import org.designup.picsou.model.initial.InitialCategories;
import org.globsframework.model.*;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.Dates;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class OfxBuilder {

  public static final String DEFAULT_ACCOUNT_ID = "00001123";

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
    InitialCategories.run(repository);
    repository.create(Key.create(Bank.TYPE, Bank.UNKNOWN_BANK_ID));
  }

  public OfxBuilder addCategory(MasterCategory master, String categoryName) {
    repository.create(Category.TYPE,
                      FieldValue.value(Category.NAME, categoryName),
                      FieldValue.value(Category.MASTER, master.getId()));
    return this;
  }

  public OfxBuilder addBankAccount(int bankEntityId, int branchId, String accountNumber, double balance, String updateDate) {
    currentAccount =
      repository.create(Key.create(Account.TYPE, repository.getIdGenerator().getNextId(Account.ID, 1)),
                        FieldValue.value(Account.BANK_ENTITY, bankEntityId),
                        FieldValue.value(Account.BRANCH_ID, branchId),
                        FieldValue.value(Account.NUMBER, accountNumber),
                        FieldValue.value(Account.BALANCE, balance),
                        FieldValue.value(Account.UPDATE_DATE, Dates.parse(updateDate)));
    return this;
  }

  public OfxBuilder addCardAccount(String cardId, double balance, String updateDate) {
    currentAccount =
      repository.create(Key.create(Account.TYPE, repository.getIdGenerator().getNextId(Account.ID, 1)),
                        FieldValue.value(Account.NUMBER, cardId),
                        FieldValue.value(Account.BALANCE, balance),
                        FieldValue.value(Account.UPDATE_DATE, Dates.parse(updateDate)),
                        FieldValue.value(Account.IS_CARD_ACCOUNT, true));
    return this;
  }

  public OfxBuilder addTransaction(String yyyyMMdd, double amount, String label) {
    return addTransactionWithNote(yyyyMMdd, amount, label, null, (MasterCategory)null);
  }

  public OfxBuilder addTransaction(String yyyyMMdd, double amount, String label, MasterCategory category) {
    return addTransactionWithNote(yyyyMMdd, amount, label, null, category);
  }

  public OfxBuilder addTransaction(String userDate, String bankDate, double amount, String label) {
    return doAddTransaction(userDate, bankDate, amount, label, null, null, null, null);
  }

  public OfxBuilder addTransaction(String yyyyMMdd, double amount, String label, String category) {
    return doAddTransaction(yyyyMMdd, null, amount, label, null, getId(category), null, null);
  }

  public OfxBuilder addTransactionWithNote(String yyyyMMdd, double amount, String label, String note) {
    return doAddTransaction(yyyyMMdd, null, amount, label, note, null, null, null);
  }

  public OfxBuilder addTransactionWithNote(String yyyyMMdd, double amount, String label, String category, String note) {
    return doAddTransaction(yyyyMMdd, null, amount, label, note, Category.findId(category, repository), null, null);
  }

  public OfxBuilder addTransactionWithNote(String yyyyMMdd, double amount, String label, String note, MasterCategory category) {
    return doAddTransaction(yyyyMMdd, null, amount, label, note, category == null ? null : category.getId(), null, null);
  }

  public OfxBuilder addDispensableTransaction(String date, double amount, String label) {
    return doAddTransaction(date, null, amount, label, null, null, null, true);
  }

  public OfxBuilder splitTransaction(String date, String label, double amount, String note, String category) {
    Date parsedDate = Dates.parse(date);

    GlobList all = repository.getAll(Transaction.TYPE,
                                     and(fieldEquals(Transaction.DAY, Month.getDay(parsedDate)),
                                         fieldEquals(Transaction.MONTH, Month.getMonthId(parsedDate)),
                                         fieldEquals(Transaction.LABEL, label),
                                         isNull(Transaction.SPLIT_SOURCE)));
    Assert.assertEquals("transaction not found", 1, all.size());
    Glob parent = all.get(0);
    doAddTransaction(date, null, amount, label, note, Category.findId(category, repository),
                     parent.get(Transaction.ID), null);

    repository.update(parent.getKey(), Transaction.SPLIT, Boolean.TRUE);
    return this;
  }

  private Integer getId(String category) {
    if (category == null) {
      return null;
    }
    Integer result = Category.findId(category, repository);
    if (result == null) {
      throw new ItemNotFound(category);
    }
    return result;
  }

  private OfxBuilder doAddTransaction(String userDate, String bankDate, double amount, String label, String note,
                                      Integer categoryId, Integer parentId, Boolean dispensable) {
    if (currentAccount == null) {
      addBankAccount(30066, 1234, DEFAULT_ACCOUNT_ID, 1.25, "2006/05/24");
    }
    if (bankDate == null) {
      bankDate = userDate;
    }
    Date parsedUserDate = Dates.parse(userDate);
    Date parsedBankDate = Dates.parse(bankDate);
    Glob transaction =
      repository.create(TYPE,
                        FieldValue.value(AMOUNT, amount),
                        FieldValue.value(MONTH, Month.getMonthId(parsedUserDate)),
                        FieldValue.value(DAY, Month.getDay(parsedUserDate)),
                        FieldValue.value(BANK_MONTH, Month.getMonthId(parsedBankDate)),
                        FieldValue.value(BANK_DAY, Month.getDay(parsedBankDate)),
                        FieldValue.value(LABEL, label),
                        FieldValue.value(ORIGINAL_LABEL, label),
                        FieldValue.value(CATEGORY, categoryId),
                        FieldValue.value(ACCOUNT, currentAccount.get(Account.ID)),
                        FieldValue.value(NOTE, note));
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

  private void writeFile(String name) {
    FileWriter writer = null;
    try {
      File file = new File(name);
      file.getParentFile().mkdirs();
      writer = new FileWriter(file);
      OfxExporter.write(repository, writer);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
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
