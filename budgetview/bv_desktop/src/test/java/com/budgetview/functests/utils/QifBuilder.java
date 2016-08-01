package com.budgetview.functests.utils;

import com.budgetview.functests.checkers.ImportDialogChecker;
import com.budgetview.functests.checkers.OperationChecker;
import com.budgetview.gui.description.Formatting;
import junit.framework.TestCase;
import org.globsframework.utils.Strings;
import org.globsframework.utils.TestUtils;

import java.io.FileWriter;
import java.io.IOException;

public class QifBuilder {
  private String fileName;
  private OperationChecker operations;
  private FileWriter writer;

  public static QifBuilder init(LoggedInFunctionalTestCase testCase) throws Exception {
    return new QifBuilder(TestUtils.getFileName(testCase, ".qif"), testCase.getOperations());
  }

  public static QifBuilder init(TestCase testCase) throws Exception {
    return new QifBuilder(TestUtils.getFileName(testCase, ".qif"), null);
  }

  private QifBuilder(String fileName, OperationChecker operations) throws Exception {
    this.operations = operations;
    this.fileName = fileName;
    writer = new FileWriter(fileName);
    writer.write("!Type:Bank");
    writer.write(Strings.LINE_SEPARATOR);
  }

  public QifBuilder addTransaction(String yyyyMMdd, double amount, String label) throws IOException {
    writer.write("D");
    writer.write(yyyyMMdd);
    writer.write(Strings.LINE_SEPARATOR);
    writer.write("T");
    writer.write(Formatting.DECIMAL_FORMAT.format(amount));
    writer.write(Strings.LINE_SEPARATOR);
    writer.write("N");
    writer.write(Strings.LINE_SEPARATOR);
    writer.write("M" + label);
    writer.write(Strings.LINE_SEPARATOR);
    writer.write("^");
    writer.write(Strings.LINE_SEPARATOR);
    return this;
  }

  public String save() throws IOException {
    writer.close();
    return fileName;
  }

  public void load(Double balance) throws IOException {
    save();
    operations.importQifFile(fileName, "Société Générale", balance);
  }

  public void load() throws IOException {
    save();
    operations.importQifFile(fileName);
  }

  public void load(int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransactionCount) throws Exception {
    save();
    ImportDialogChecker importDialog = operations.openImportDialog()
      .setFilePath(fileName)
      .acceptFile();
    if (importDialog.accountIsEditable()) {
      importDialog.setMainAccount();
    }
    importDialog.completeImport(importedTransactionCount, ignoredTransactionCount, autocategorizedTransactionCount);
  }

  public void loadInAccount(String accountName) throws Exception {
    save();
    ImportDialogChecker importDialog = operations.openImportDialog()
      .setFilePath(fileName)
      .acceptFile()
      .selectAccount(accountName);
    importDialog.completeImport();
  }

  public void loadFirstStartingAtZero(double amount) throws IOException {
    save();
    operations.openImportDialog().setFilePath(fileName)
      .acceptFile()
      .selectNewAccount()
      .setAccountName("Main account")
      .setMainAccount()
      .selectBank("Société Générale")
      .completeImportStartFromZero(amount);
  }
}
