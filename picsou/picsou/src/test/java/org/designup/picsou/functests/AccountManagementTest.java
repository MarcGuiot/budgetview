package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.MasterCategory;
import static org.designup.picsou.model.MasterCategory.*;
import static org.designup.picsou.model.TransactionType.*;

public class AccountManagementTest extends LoggedInFunctionalTestCase {

  public void runBare() throws Throwable {
    System.out.println(getClass().getSimpleName() + "." + getName() +
                       " DISABLED - account view redesign in progress");
  }

  public void testSingleAccount() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 1.23, "2006/01/30")
      .addTransaction("2006/01/10", -1, "Blah")
      .load();

    accounts.assertDisplayEquals("Carte 10101010", 1.23, "2006/01/30");
    accounts.initContent()
      .add("Tous les comptes", 1.23, "2006/01/30")
      .add("Compte 10101010", 1.23, "2006/01/30")
      .check();
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

    accounts.initContent()
      .add("Tous les comptes", 10.0, "2006/01/29")
      .add("Compte 123123123", 2.35, "2006/01/31")
      .add("Carte 1000-2000-3000-4000", 7.65, "2006/01/29")
      .check();
    transactions.initContent()
      .add("20/01/2006", CREDIT_CARD, "Bar", "", -6, NONE)
      .add("17/01/2006", CREDIT_CARD, "Foo", "", -3, NONE)
      .add("15/01/2006", PRELEVEMENT, "GDF", "", -10, MasterCategory.HOUSE)
      .add("10/01/2006", PRELEVEMENT, "EDF", "", -1, MasterCategory.HOUSE)
      .check();
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

    accounts.assertDisplayEquals("Carte 10101010", 12345.60, "2006/05/30");
    accounts.initContent()
      .add("Tous les comptes", 12345.60, "2006/05/30")
      .add("Compte 10101010", 12345.60, "2006/05/30")
      .check();

    periods.selectCells(0, 4);

    transactions.initContent()
      .add("01/05/2006", PRELEVEMENT, "Foo", "", -10, NONE)
      .add("10/01/2006", PRELEVEMENT, "Bar", "", -20, NONE)
      .check();
  }

  public void testFilteringWithMultipleAccounts() throws Exception {
    learn("EDF", MasterCategory.HOUSE);
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "123123123", 2.35, "2006/01/30")
      .addTransaction("2006/01/10", -1, "EDF")
      .addCardAccount("1000200030004000", 7.65, "2006/01/28")
      .addTransaction("2006/01/17", -3, "Foo")
      .load();

    accounts.initContent()
      .add("Tous les comptes", 10.00, "2006/01/28")
      .add("Compte 123123123", 2.35, "2006/01/30")
      .add("Carte 1000-2000-3000-4000", 7.65, "2006/01/28")
      .check();
    transactions.initContent()
      .add("17/01/2006", CREDIT_CARD, "Foo", "", -3, NONE)
      .add("10/01/2006", PRELEVEMENT, "EDF", "", -1, MasterCategory.HOUSE)
      .check();
    categories.initContent()
      .add(ALL, 0, 0, -4.0, 1.0)
      .add(NONE, 0, 0, 3.0, 0.75)
      .add(MasterCategory.HOUSE, 0, 0, -1.0, 0.25)
      .check();

    accounts.select("Carte");
    accounts.assertDisplayEquals("Carte 1000-2000-3000-4000", 7.65, "2006/01/28");
    categories.initContent()
      .add(ALL, 0, 0, -3.0, 1.0)
      .add(NONE, 0, 0, 3.0, 1.0)
      .check();
    transactions.initContent()
      .add("17/01/2006", CREDIT_CARD, "Foo", "", -3, NONE)
      .check();

    accounts.select("Compte 123123123");
    accounts.assertDisplayEquals("Compte 123123123", 2.35, "2006/01/30");
    transactions.initContent()
      .add("10/01/2006", PRELEVEMENT, "EDF", "", -1, MasterCategory.HOUSE)
      .check();
    categories.initContent()
      .add(ALL, 0, 0, -1.0, 1.0)
      .add(MasterCategory.HOUSE, 0, 0, -1.0, 1.0)
      .check();

    accounts.select("Tous les comptes");
    transactions.initContent()
      .add("17/01/2006", CREDIT_CARD, "Foo", "", -3, NONE)
      .add("10/01/2006", PRELEVEMENT, "EDF", "", -1, MasterCategory.HOUSE)
      .check();
    categories.initContent()
      .add(ALL, 0, 0, -4.0, 1.0)
      .add(NONE, 0, 0, 3.0, 0.75)
      .add(MasterCategory.HOUSE, 0, 0, -1.0, 0.25)
      .check();
  }

  public void testAccountSelectionPreservesPeriodAndCategorySelection() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "123123123", 2.35, "2006/01/30")
      .addTransaction("2006/01/10", -1, "EDF")
      .addTransaction("2006/02/10", -2, "GDF")
      .addCardAccount("1000200030004000", 7.65, "2006/01/28")
      .addTransaction("2006/01/17", -3, "Foo")
      .addTransaction("2006/02/17", -6, "Blah")
      .load();

    accounts.initContent()
      .add("Tous les comptes", 10.00, "2006/01/28")
      .add("Compte 123123123", 2.35, "2006/01/30")
      .add("Carte 1000-2000-3000-4000", 7.65, "2006/01/28")
      .check();

    periods.assertContains("2006/01 (0.00/4.00)", "2006/02 (0.00/8.00)");
    periods.assertCellSelected(1);

    periods.selectCell(0);
    periods.assertCellSelected(true, false);
    categories.select(MasterCategory.CLOTHING, MasterCategory.HOUSE);

    accounts.select("Carte");
    periods.assertCellSelected(true, false);
    categories.assertSelectionEquals(MasterCategory.CLOTHING, MasterCategory.HOUSE);

    accounts.select("Tous les comptes");
    periods.assertCellSelected(0);
    categories.assertSelectionEquals(MasterCategory.CLOTHING, MasterCategory.HOUSE);
  }

  public void testNothingShownForQifFiles() throws Exception {
    QifBuilder.init(this)
      .addTransaction("2006/01/01", 12.35, "foo")
      .addTransaction("2006/02/01", -7.50, "foo")
      .load(null);

    accounts.assertDisplayEquals("Compte principal");
  }
}
