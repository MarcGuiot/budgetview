package org.designup.picsou.functests.categorization;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SavingsCategorizationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2008/06/30");
    super.setUp();
  }

  public void testSavingsOperationsCanBeAssignedToOtherBudgetAreas() throws Exception {

    operations.openPreferences().setFutureMonthsCount(4).validate();

    OfxBuilder
      .init(this)
      .addBankAccount("000001", 0.00, "2008/06/30")
      .addTransaction("2008/06/15", -1000.00, "EXPENSE")
      .addTransaction("2008/06/30", +5.00, "INTEREST")
      .load();
    mainAccounts.edit("Account n. 000001")
      .setName("Savings1")
      .setAsSavings()
      .validate();

    OfxBuilder
      .init(this)
      .addBankAccount("000002", 0.00, "2008/06/30")
      .addTransaction("2008/06/25", -200.00, "TRANSFER FROM MAIN2")
      .load();
    mainAccounts.edit("Account n. 000002")
      .setName("Main2")
      .setAsSavings()
      .validate();

    OfxBuilder
      .init(this)
      .addBankAccount("000003", 0.00, "2008/06/30")
      .addTransaction("2008/06/25", 200.00, "TRANSFER TO MAIN3")
      .load();
    mainAccounts.edit("Account n. 000003")
      .setName("Main3")
      .validate();

    categorization.selectTransaction("INTEREST")
      .selectIncome()
      .checkContainsNoSeries()
      .createSeries()
      .checkReadOnlyTargetAccount("Savings1")
      .setName("Interest on savings")
      .setAmount(5.00)
      .setPropagationEnabled()
      .validate();

    categorization.initContent()
      .add("15/06/2008", "", "EXPENSE", -1000.00)
      .add("30/06/2008", "Interest on savings", "INTEREST", 5.00)
      .add("25/06/2008", "", "TRANSFER FROM MAIN2", -200.00)
      .add("25/06/2008", "", "TRANSFER TO MAIN3", 200.00)
      .check();

    timeline.selectMonths(200806, 200807, 200808, 200809);
    transactions.showPlannedTransactions();
    transactions.initAmountContent()
      .add("04/09/2008", "Planned: Interest on savings", 5.00, "Interest on savings", 15.00, 15.00, "Savings1")
      .add("04/08/2008", "Planned: Interest on savings", 5.00, "Interest on savings", 10.00, 10.00, "Savings1")
      .add("04/07/2008", "Planned: Interest on savings", 5.00, "Interest on savings", 5.00, 5.00, "Savings1")
      .add("30/06/2008", "INTEREST", 5.00, "Interest on savings", 0.00, 0.00, "Savings1")
      .add("25/06/2008", "TRANSFER TO MAIN3", 200.00, "To categorize", 0.00, 0.00, "Main3")
      .add("25/06/2008", "TRANSFER FROM MAIN2", -200.00, "To categorize", 0.00, -5.00, "Main2")
      .add("15/06/2008", "EXPENSE", -1000.00, "To categorize", -5.00, 195.00, "Savings1")
      .check();

    categorization
      .selectTransaction("TRANSFER TO MAIN3")
      .selectTransfers()
      .checkContainsNoSeries();

    categorization
      .selectTransfers()
      .createSeries()
      .setName("Transfer 1")
      .checkToAccount("Main3")
      .setFromAccount("Main2")
      .setAmount(200.00)
      .validate();

    categorization
      .selectTransaction("TRANSFER FROM MAIN2")
      .selectTransfers()
      .checkContainsSeries("Transfer 1")
      .checkSeriesNotSelected("Transfer 1")
      .selectSeries("Transfer 1");

    transactions.initAmountContent()
      .add("27/09/2008", "Planned: Transfer 1", 200.00, "Transfer 1", 600.00, 600.00, "Main3")
      .add("27/09/2008", "Planned: Transfer 1", -200.00, "Transfer 1", -600.00, -585.00, "Main2")
      .add("04/09/2008", "Planned: Interest on savings", 5.00, "Interest on savings", 15.00, -385.00, "Savings1")
      .add("27/08/2008", "Planned: Transfer 1", 200.00, "Transfer 1", 400.00, 400.00, "Main3")
      .add("27/08/2008", "Planned: Transfer 1", -200.00, "Transfer 1", -400.00, -390.00, "Main2")
      .add("04/08/2008", "Planned: Interest on savings", 5.00, "Interest on savings", 10.00, -190.00, "Savings1")
      .add("27/07/2008", "Planned: Transfer 1", 200.00, "Transfer 1", 200.00, 200.00, "Main3")
      .add("27/07/2008", "Planned: Transfer 1", -200.00, "Transfer 1", -200.00, -195.00, "Main2")
      .add("04/07/2008", "Planned: Interest on savings", 5.00, "Interest on savings", 5.00, 5.00, "Savings1")
      .add("30/06/2008", "INTEREST", 5.00, "Interest on savings", 0.00, 0.00, "Savings1")
      .add("25/06/2008", "TRANSFER TO MAIN3", 200.00, "Transfer 1", 0.00, 0.00, "Main3")
      .add("25/06/2008", "TRANSFER FROM MAIN2", -200.00, "Transfer 1", 0.00, -5.00, "Main2")
      .add("15/06/2008", "EXPENSE", -1000.00, "To categorize", -5.00, 195.00, "Savings1")
      .check();
  }

  public void testCannotCreateNewSeriesWithSavingsOperationsAndDifferentAccountsIfNotTransferBudgetArea() throws Exception {

    OfxBuilder
      .init(this)
      .addBankAccount("000001", 0.00, "2008/06/30")
      .addTransaction("2008/06/30", 200.00, "TRANSFER IN MAIN1")
      .load();
    mainAccounts.edit("Account n. 000001")
      .setName("Main1")
      .setAsMain()
      .validate();

    OfxBuilder
      .init(this)
      .addBankAccount("000002", 0.00, "2008/06/30")
      .addTransaction("2008/06/30", -1000.00, "EXPENSE A IN SAVINGS2")
      .addTransaction("2008/06/30", -50.00, "EXPENSE B IN SAVINGS2")
      .addTransaction("2008/06/30", -200.00, "TRANSFER IN SAVINGS2")
      .load();
    mainAccounts.edit("Account n. 000002")
      .setName("Savings2")
      .setAsSavings()
      .validate();

    OfxBuilder
      .init(this)
      .addBankAccount("000003", 0.00, "2008/06/30")
      .addTransaction("2008/06/28", -100.00, "EXPENSE IN SAVINGS3")
      .load();
    mainAccounts.edit("Account n. 000003")
      .setName("Savings3")
      .setAsSavings()
      .validate();

    // 1. Main + Savings combination denied

    categorization
      .selectTransactions("TRANSFER IN MAIN1", "TRANSFER IN SAVINGS2")
      .selectIncome()
      .checkCreateSeriesMessage("Transactions from main and savings accounts must be assigned to " +
                                "different envelopes.");

    // 2. Savings + Savings combination denied

    categorization
      .selectTransactions("TRANSFER IN SAVINGS2", "EXPENSE IN SAVINGS3")
      .selectIncome()
      .checkCreateSeriesMessage("Transactions from different savings accounts must be assigned to " +
                                "different envelopes.");

    // 3. Can create only for a single account

    categorization
      .selectTransactions("EXPENSE A IN SAVINGS2", "EXPENSE B IN SAVINGS2")
      .selectRecurring()
      .createSeries()
      .setName("Interest for savings")
      .setAmount(50.00)
      .validate();

    categorization.initContent()
      .add("30/06/2008", "Interest for savings", "EXPENSE A IN SAVINGS2", -1000.00)
      .add("30/06/2008", "Interest for savings", "EXPENSE B IN SAVINGS2", -50.00)
      .add("28/06/2008", "", "EXPENSE IN SAVINGS3", -100.00)
      .add("30/06/2008", "", "TRANSFER IN MAIN1", 200.00)
      .add("30/06/2008", "", "TRANSFER IN SAVINGS2", -200.00)
      .check();
  }

}
