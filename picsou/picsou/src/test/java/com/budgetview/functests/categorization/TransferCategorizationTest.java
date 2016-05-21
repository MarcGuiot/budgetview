package com.budgetview.functests.categorization;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;

public class TransferCategorizationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2008/06/30");
    super.setUp();
  }

  public void testOnlyAllowsMultiSelectionOfSymetricalTransactionsForCreatingATransferSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("00001", 0.00, "2008/06/30")
      .addTransaction("2008/06/15", +50.00, "MAIN1 POS")
      .addTransaction("2008/06/30", -50.00, "MAIN1 NEG")
      .load();
    mainAccounts.edit("Account n. 00001")
      .setName("Main1")
      .validate();

    OfxBuilder
      .init(this)
      .addBankAccount("00002", 0.00, "2008/06/30")
      .addTransaction("2008/06/15", +50.00, "MAIN2 POS")
      .addTransaction("2008/06/15", +40.00, "MAIN2 POS2")
      .addTransaction("2008/06/30", -50.00, "MAIN2 NEG")
      .load();
    mainAccounts.edit("Account n. 00002")
      .setName("Main2")
      .validate();

    OfxBuilder
      .init(this)
      .addBankAccount("00003", 0.00, "2008/06/30")
      .addTransaction("2008/06/15", +50.00, "MAIN3 POS")
      .addTransaction("2008/06/30", -50.00, "MAIN3 NEG")
      .load();
    mainAccounts.edit("Account n. 00003")
      .setName("Main3")
      .validate();

    categorization.selectTransactions("MAIN1 POS", "MAIN2 POS", "MAIN3 POS")
      .selectTransfers()
      .checkCreateSeriesMessage("Two accounts max for a transfer");

    categorization.selectTransactions("MAIN1 POS", "MAIN2 POS")
      .selectTransfers()
      .checkCreateSeriesMessage("Signs incompatible with valid transfer");

    categorization.selectTransactions("MAIN1 NEG", "MAIN2 NEG")
      .selectTransfers()
      .checkCreateSeriesMessage("Signs incompatible with valid transfer");

    categorization.selectTransactions("MAIN1 NEG", "MAIN1 POS", "MAIN2 NEG")
      .selectTransfers()
      .checkCreateSeriesMessage("Signs incompatible with valid transfer");

    categorization.selectTransactions("MAIN1 NEG", "MAIN2 POS", "MAIN2 NEG")
      .selectTransfers()
      .checkCreateSeriesMessage("Signs incompatible with valid transfer");

    categorization.selectTransactions("MAIN1 NEG", "MAIN2 POS")
      .selectTransfers()
      .createSeries()
      .setName("Transfer")
      .checkFromAccount("Main1")
      .checkToAccount("Main2")
      .setAmount(50.00)
      .validate();
  }

  public void testMessagesDisplayedWhenNoSeriesAvailable() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();

    categorization
      .selectTransactions("Virement")
      .selectTransfers()
      .checkNoSeriesMessage("There are no transfer series");

    accounts
      .createNewAccount()
      .checkIsMain()
      .setAsSavings()
      .setName("Epargne")
      .selectBank("CIC")
      .setPosition(0.00)
      .validate();

    categorization
      .selectTransactions("Virement")
      .selectTransfers()
      .checkNoSeriesMessage("There are no transfer series");

    budgetView.transfer.createSeries()
      .setName("My savings")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Epargne")
      .validate();

    categorization
      .selectTransfers()
      .checkNoSeriesMessageHidden();

    budgetView.transfer.editSeries("My savings")
      .deleteCurrentSeries();

    categorization
      .selectTransactions("Virement")
      .selectTransfers()
      .checkNoSeriesMessage("There are no transfer series");

    savingsAccounts.edit("Epargne").openDelete().validate();

    categorization
      .selectTransactions("Virement")
      .selectTransfers()
      .checkNoSeriesMessage("There are no transfer series.");
  }
}
