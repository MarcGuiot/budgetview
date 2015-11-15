package org.designup.picsou.functests.categorization;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class TransferCategorizationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2008/06/30");
    super.setUp();
  }

  public void testSavingsHelpDisplayedWhenNoSavingsAccountAvailable() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();

    views.selectCategorization();

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
