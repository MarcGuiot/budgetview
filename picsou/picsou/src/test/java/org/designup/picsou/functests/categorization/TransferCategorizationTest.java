package org.designup.picsou.functests.categorization;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class TransferCategorizationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2008/06/30");
    super.setUp();
  }

  public void testCreatedSavingsTransactionsAreNotVisibleInCategorization() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    timeline.selectAll();
    views.selectHome();
    views.selectCategorization();
    categorization
      .selectTransactions("Virement")
      .selectTransfers();

    categorization.createAccount()
      .setName("Epargne")
      .selectBank("ING Direct")
      .checkIsSavings()
      .checkAccountTypeNotEditable()
      .setPosition(1000.0)
      .validate();

    categorization
      .selectTransfers()
      .createSeries()
      .setFromAccount("Account n. 00001123")
      .setToAccount("Epargne")
      .setName("Epargne")
      .validate();

    categorization.initContent()
      .add("10/08/2008", "Epargne", "Virement", -100)
      .check();
  }

  public void testSavingsHelpDisplayedWhenNoSavingsAccountAvailable() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();

    views.selectCategorization();

    categorization
      .selectTransactions("Virement")
      .selectTransfers()
      .checkNoSeriesMessage("No savings account is declared")
      .clickSeriesMessageAccountCreationLink("create a savings account")
      .checkIsSavings()
      .setName("Epargne")
      .selectBank("CIC")
      .setPosition(0.00)
      .validate();

    views.selectBudget();
//    budgetView.savings.editSeries("From account Epargne").deleteCurrentSeries();
//    budgetView.savings.editSeries("To account Epargne").deleteCurrentSeries();

    views.selectCategorization();
    categorization
      .selectTransactions("Virement")
      .selectTransfers()
      .checkNoSeriesMessage("There are no transfer series");

    views.selectBudget();
    budgetView.transfer.createSeries()
      .setName("My savings")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Epargne")
      .validate();

    views.selectCategorization();
    categorization
      .selectTransfers()
      .checkNoSeriesMessageHidden();

    views.selectBudget();
    budgetView.transfer.editSeries("My savings")
      .deleteCurrentSeries();

    views.selectCategorization();
    categorization
      .selectTransactions("Virement")
      .selectTransfers()
      .checkNoSeriesMessage("There are no transfer series");

    views.selectHome();
    savingsAccounts.edit("Epargne").openDelete().validate();

    views.selectCategorization();
    categorization
      .selectTransactions("Virement")
      .selectTransfers()
      .checkNoSeriesMessage("No savings account is declared");
  }

  public void testCreateSavingsAccountActionAvailableInSavingsBlock() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/25", -50.0, "ING")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0)
      .selectTransfers()
      .createSavingsAccount()
      .checkIsSavings()
      .checkAccountTypeNotEditable()
      .setName("Epargne ING")
      .selectBank("ING Direct")
      .setPosition(200.00)
      .validate();

    views.selectHome();
    savingsAccounts.edit("Epargne ING")
      .checkSelectedBank("ING Direct")
      .validate();
  }

  public void testSavingsCategorizationMessage() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("000001", 0.00, "2008/06/30")
      .addTransaction("2008/06/30", -100.00, "SAVINGS 1.1")
      .addTransaction("2008/06/30", -100.00, "SAVINGS 1.2")
      .load();
    mainAccounts.edit("Account n. 000001")
      .setAsSavings()
      .validate();

    OfxBuilder
      .init(this)
      .addBankAccount("000002", 0.00, "2008/06/30")
      .addTransaction("2008/06/30", -200.00, "SAVINGS 2")
      .load();
    mainAccounts.edit("Account n. 000002")
      .setAsSavings()
      .validate();

    OfxBuilder
      .init(this)
      .addBankAccount("000003", 0.00, "2008/06/30")
      .addTransaction("2008/06/30", -200.00, "OTHER")
      .load();

    categorization.selectTransaction("OTHER");
    categorization.selectTransfers().checkMessageHidden();

    categorization.selectTransaction("SAVINGS 1.1");
    categorization.checkTransfersPreSelected()
      .getSavings()
      .checkMessage("This operation is part of a savings account. Edit this account.");

    categorization.selectTransactions("SAVINGS 1.1", "SAVINGS 1.2");
    categorization.checkTransfersPreSelected()
      .getSavings()
      .checkMessage("These operations are part of a savings account. Edit this account.");

    categorization.selectTransactions("SAVINGS 1.1", "SAVINGS 1.2");
    categorization.checkTransfersPreSelected()
      .getSavings()
      .checkMessage("These operations are part of a savings account. Edit this account.");

    categorization.selectTransactions("SAVINGS 1.1", "SAVINGS 2");
    categorization.checkTransfersPreSelected()
      .getSavings()
      .checkMessage("These operations are part of several savings accounts.");

    categorization.selectTransaction("SAVINGS 1.1");

    categorization.checkTransfersPreSelected()
      .checkAllButTransferBudgetAreaAreDisabled()
      .getSavings()
      .checkMessage("This operation is part of a savings account. Edit this account.")
      .clickMessageToEditAccount("Edit this account.")
      .setAsMain()
      .validate();

    categorization.checkTransfersPreSelected();
    categorization.checkAllBudgetAreasAreEnabled();

    categorization.selectVariable().selectNewSeries("Misc");
    categorization.checkTable(new Object[][]{
      {"30/06/2008", "", "OTHER", -200.0},
      {"30/06/2008", "Misc", "SAVINGS 1.1", -100.0},
      {"30/06/2008", "", "SAVINGS 1.2", -100.0},
      {"30/06/2008", "", "SAVINGS 2", -200.0}
    });
  }
}
