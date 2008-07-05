package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.ImportChecker;
import org.designup.picsou.functests.checkers.OperationChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.MasterCategory;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

public class AccountManagementTest extends LoggedInFunctionalTestCase {

  public void testSingleAccount() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 1.23, "2006/01/30")
      .addTransaction("2006/01/10", -1, "Blah")
      .load();

    views.selectHome();
    accounts.assertDisplayEquals("10101010", 1.23, "2006/01/30");
    accounts.checkSummary(1.23, "2006/01/30");
  }

  public void testAccountsAreUpdatedDuringSubsequentImports() throws Exception {
    learn("EDF", MasterCategory.HOUSE);
    learn("GDF", MasterCategory.HOUSE);
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "123123123", 0.20, "2006/01/30")
      .addTransaction("2006/01/10", -1, "EDF")
      .addCardAccount("1000200030004000", 321.54, "2006/01/28")
      .addTransaction("2006/01/17", -3, "Foo")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "123123123", 2.35, "2006/01/31")
      .addTransaction("2006/01/15", -10, "GDF")
      .addCardAccount("1000200030004000", 7.65, "2006/01/29")
      .addTransaction("2006/01/20", -6, "Bar")
      .load();

    views.selectHome();
    accounts.checkSummary(10.0, "2006/01/29");
    accounts.assertDisplayEquals("Compte 123123123", 2.35, "2006/01/31");
    accounts.assertDisplayEquals("Carte 1000-2000-3000-4000", 7.65, "2006/01/29");
  }

  public void testOnlyTheLatestAccountBalanceIsTakenIntoAccount() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 12345.60, "2006/05/30")
      .addTransaction("2006/05/01", -10, "Foo")
      .load();
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 1111, "2006/01/30")
      .addTransaction("2006/01/10", -20, "Bar")
      .load();

    views.selectHome();
    accounts.assertDisplayEquals("10101010", 12345.60, "2006/05/30");
    accounts.checkSummary(12345.60, "2006/05/30");
  }

  public void testNothingShownForQifFiles() throws Exception {
    String path = QifBuilder.init(this)
      .addTransaction("2006/01/01", 12.35, "foo")
      .addTransaction("2006/02/01", -7.50, "foo")
      .save();
    operations.importQifFiles(10, "Societe generale", path);

    views.selectHome();
    accounts.assertDisplayEquals("Main account");
  }

  public void testImportFromViewInitializesTheBankURL() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 12345.60, "2006/05/30")
      .addTransaction("2006/05/01", -10, "Foo")
      .load();

    views.selectHome();
    Window dialog = WindowInterceptor.getModalDialog(
      accounts.getImportTrigger("10101010"));
    ImportChecker importer = new ImportChecker(dialog);
    importer.checkbank("Societe Generale");
  }

  public void testImportFromViewInitializesTheDefaultBankAndAccountForQifFiles() throws Exception {
    String path = QifBuilder.init(this)
      .addTransaction("2006/01/01", 12.35, "foo")
      .addTransaction("2006/02/01", -7.50, "foo")
      .save();

    operations.importQifFiles(10, "Societe generale", path);

    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 12345.60, "2006/05/30")
      .addTransaction("2006/05/01", -10, "Foo")
      .load();

    views.selectHome();
    Window dialog = WindowInterceptor.getModalDialog(
      accounts.getImportTrigger(OperationChecker.DEFAULT_ACCOUNT_NUMBER));
    ImportChecker importer = new ImportChecker(dialog);
    importer.checkbank("Societe Generale");
    importer.selectFiles(path);
    importer.startImport();
    importer.checkSelectedAccount("Main account(11111)");
  }
}
