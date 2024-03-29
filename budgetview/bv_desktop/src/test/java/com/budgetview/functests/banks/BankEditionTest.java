package com.budgetview.functests.banks;

import com.budgetview.functests.checkers.AccountEditionChecker;
import com.budgetview.functests.checkers.BankChooserChecker;
import com.budgetview.functests.checkers.ImportDialogPreviewChecker;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.functests.utils.QifBuilder;
import com.budgetview.model.TransactionType;
import org.junit.Test;

public class BankEditionTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    resetWindow();
    setCurrentDate("2008/08/30");
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  protected void tearDown() throws Exception {
    resetWindow();
    super.tearDown();
  }

  @Test
  public void testCanAddBankFromImportDialog() throws Exception {

    /* -- Create bank and load first file -- */

    String ofxFilePath = OfxBuilder.init(this)
      .addBankAccount(777777, 7777, "0001234", 1000.00, "2008/06/16")
      .addTransaction("2008/06/16", -27.50, "Burger King")
      .save();

    ImportDialogPreviewChecker importDialog = operations.openImportDialog()
      .importFileAndPreview(ofxFilePath)
      .setAccountName("TestAccount");

    BankChooserChecker bankChooser = importDialog.openBankSelection();

    bankChooser
      .addNewBank()
      .setName("")
      .checkValidationError("You must enter a name for this bank")
      .setName("TestBank")
      .checkNoErrorDisplayed()
      .setUrl("http://www.testbank.net")
      .validate();

    bankChooser
      .checkContainsBanks("TestBank", "BNP Paribas")
      .checkSelectedBank("TestBank")
      .validate();

    importDialog
      .checkSelectedBank("TestBank")
      .setMainAccount()
      .checkTransactions(new Object[][]{
        {"2008/06/16", "Burger King", "-27.50"}
      })
      .importAccountAndComplete();

    mainAccounts.checkAccount("TestAccount", 1000.00, "2008/06/16");
    mainAccounts.checkAccountWebsite("TestAccount", "TestBank", "http://www.testbank.net");

    /* -- Restart -- */

    restartApplication();

    mainAccounts.checkAccount("TestAccount", 1000.00, "2008/06/16");
    mainAccounts.checkAccountWebsite("TestAccount", "TestBank", "http://www.testbank.net");

    /* -- Load second file -- */

    String secondOfxFilePath = OfxBuilder.init(this)
      .addBankAccount(777777, 7777, "0001234", 970.00, "2008/07/16")
      .addTransaction("2008/07/16", -30.00, "Burger King")
      .save();

    ImportDialogPreviewChecker secondImportDialog = operations.openImportDialog()
      .importFileAndPreview(secondOfxFilePath)
      .checkSelectedAccount("TestAccount")
      .selectNewAccount();

    secondImportDialog.openBankSelection()
      .checkContainsBanks("TestBank")
      .validate();

    secondImportDialog
      .selectAccount("TestAccount")
      .importAccountAndComplete();
  }

  @Test
  public void testEditingUserBanksFromImportDialog() throws Exception {

    String ofxFilePath = OfxBuilder.init(this)
      .addBankAccount(777777, 7777, "0001234", 1000.00, "2008/06/16")
      .addTransaction("2008/06/16", -27.50, "Burger King")
      .save();

    ImportDialogPreviewChecker importDialog = operations.openImportDialog()
      .importFileAndPreview(ofxFilePath)
      .setAccountName("TestAccount");

    BankChooserChecker bankChooser = importDialog.openBankSelection();

    /* ----- Create the bank ----- */

    bankChooser
      .addNewBank()
      .checkTitle("Add a bank")
      .checkName("")
      .checkUrl("")
      .setName("TestBank")
      .setUrlAndValidate("http://www.testbank.net");

    bankChooser
      .checkSelectedBank("TestBank")
      .selectBank("CIC")
      .checkEditDisabled()
      .selectBank("TestBank");

    /* ----- Change the bank ----- */

    bankChooser.edit()
      .checkTitle("Edit bank")
      .checkName("TestBank")
      .checkUrl("http://www.testbank.net")
      .setName("NewBank")
      .setUrlAndValidate("http://www.newbank.net");

    bankChooser
      .checkBankNotPresent("TestBank")
      .checkContainsBanks("NewBank", "CIC")
      .checkSelectedBank("NewBank")
      .validate();

    importDialog
      .setMainAccount()
      .checkTransactions(new Object[][]{
        {"2008/06/16", "Burger King", "-27.50"}
      })
      .importAccountAndComplete();

    mainAccounts.checkAccountWebsite("TestAccount", "NewBank", "http://www.newbank.net");

    /* ----- Try to delete the bank when it is used ----- */

    ImportDialogPreviewChecker secondImportDialog = operations.openImportDialog()
      .importFileAndPreview(ofxFilePath)
      .selectNewAccount();

    secondImportDialog.openBankSelection()
      .selectBank("CIC")
      .checkDeleteDisabled()
      .selectBank("NewBank")
      .checkDeleteRejected("Delete bank", "This bank is used by account TestAccount. You cannot delete it.")
      .validate();

    secondImportDialog.close();

    mainAccounts.edit("TestAccount")
      .selectBank("CIC")
      .validate();

    /* ----- Delete the bank ----- */

    ImportDialogPreviewChecker thirdImportDialog = operations.openImportDialog()
      .setFilePath(OfxBuilder.init(this)
                     .addBankAccount(777777, 7777, "0001234", 1000.00, "2008/06/18")
                     .addTransaction("2008/06/18", -15.00, "Mc Do")
                     .save())
      .importFileAndPreview()
      .selectNewAccount();

    thirdImportDialog.openBankSelection()
      .selectCountry("All")
      .selectBank("NewBank")
      .deleteAndCancel()
      .checkContainsBanks("NewBank")
      .delete("Delete bank", "This bank is not used. Do you want to delete it?")
      .checkBankNotPresent("NewBank")
      .selectBank("CIC")
      .validate();

    thirdImportDialog
      .selectAccount("TestAccount")
      .importAccountAndComplete();

    mainAccounts.edit("TestAccount")
      .checkSelectedBank("CIC")
      .checkBankNotPresentInList("NewBank")
      .validate();

    mainAccounts.checkAccountWebsite("TestAccount", "CIC", "http://www.cic.fr");
  }

  @Test
  public void testEditingBanksFromAccountView() throws Exception {

    accounts.createNewAccount()
      .setName("TestAccount")
      .selectNewBank("TestBank", "http://www.testbank.net")
      .setPosition(100.00)
      .validate();

    accounts.createNewAccount()
      .setName("OtherAccount")
      .selectBank("TestBank")
      .setPosition(100.00)
      .validate();

    mainAccounts.checkAccountWebsite("TestAccount", "TestBank", "http://www.testbank.net");
    mainAccounts.checkAccountWebsite("OtherAccount", "TestBank", "http://www.testbank.net");

    mainAccounts.edit("TestAccount")
      .modifyBank("TestBank", "NewBank", "http://www.newbank.net")
      .validate();

    mainAccounts.checkAccountWebsite("TestAccount", "NewBank", "http://www.newbank.net");
    mainAccounts.checkAccountWebsite("OtherAccount", "NewBank", "http://www.newbank.net");

    /* ------ Try to delete the bank while it is used by another account -- */

    mainAccounts.edit("TestAccount")
      .checkSelectedBank("NewBank")
      .checkDeleteBankRejected("Delete bank", "This bank is used by account OtherAccount. You cannot delete it.")
      .cancel();

    mainAccounts.edit("OtherAccount")
      .selectBank("CIC")
      .validate();

    AccountEditionChecker accountEdition = mainAccounts.edit("TestAccount");

    accountEdition
      .openBankSelection()
      .checkSelectedBank("NewBank")
      .delete()
      .checkNoBankSelected()
      .checkValidateDisabled()
      .selectBank("BNP Paribas")
      .validate();

    accountEdition.validate();

    mainAccounts.checkAccountWebsite("TestAccount", "BNP Paribas", "http://www.bnpparibas.net");
    mainAccounts.checkAccountWebsite("OtherAccount", "CIC", "http://www.cic.fr");

    mainAccounts.edit("OtherAccount")
      .checkBankNotPresentInList("NewBank")
      .cancel();
  }

  @Test
  public void testCancelEditionAndDeletion() throws Exception {
    accounts.createNewAccount()
      .setName("TestAccount")
      .selectNewBank("TestBank", "http://www.testbank.net")
      .setPosition(100.00)
      .validate();

    mainAccounts.checkAccountWebsite("TestAccount", "TestBank", "http://www.testbank.net");

    mainAccounts.edit("TestAccount")
      .modifyBank("TestBank", "NewBank", "http://www.newbank.net")
      .checkSelectedBank("NewBank")
      .cancel();

    mainAccounts.checkAccountWebsite("TestAccount", "TestBank", "http://www.testbank.net");

    mainAccounts.edit("TestAccount")
      .deleteBankAndSelect("CIC")
      .checkBankNotPresentInList("TestBank")
      .checkSelectedBank("CIC")
      .cancel();

    mainAccounts.checkAccountWebsite("TestAccount", "TestBank", "http://www.testbank.net");

    mainAccounts.edit("TestAccount")
      .modifyBank("TestBank", "NewBank", "http://www.newbank.net")
      .checkSelectedBank("NewBank")
      .validate();

    mainAccounts.checkAccountWebsite("TestAccount", "NewBank", "http://www.newbank.net");
  }

  @Test
  public void testCreatingABankInFileImportPreview() throws Exception {
    String qifPath = QifBuilder.init(this)
      .addTransaction("2008/08/30", -50.00, "MacDo")
      .save();

    operations.openImportDialog()
      .importFileAndPreview(qifPath)
      .setAccountName("NewAccount")
      .addNewAccountBank("NewBank", "http://www.newbank.net")
      .setMainAccount()
      .checkTransactions(new Object[][]{
        {"2008/08/30", "MacDo", "-50.00"},
      })
      .setPosition(1000.00)
      .importAccountAndComplete();

    mainAccounts.checkAccountWebsite("NewAccount", "NewBank", "http://www.newbank.net");

    transactions.initContent()
      .add("30/08/2008", TransactionType.PRELEVEMENT, "MACDO", "", -50.00)
      .check();
  }

  @Test
  public void testBanksAreFilteredByCountry() throws Exception {

    String qifPath = QifBuilder.init(this)
      .addTransaction("2008/08/30", -50.00, "MacDo")
      .save();

    ImportDialogPreviewChecker importDialog = operations.openImportDialog()
      .importFileAndPreview(qifPath);
    importDialog.openBankSelection()
      .checkCountry("All")
      .checkContainsBanks("CIC", "Credit Suisse", "BNP Paribas Fortis")
      .cancel();
    importDialog.close();

    accounts.createNewAccount()
      .setName("Account 1")
      .setAsMain()
      .selectBank("Credit Suisse")
      .setPosition(100.00)
      .validate();

    importDialog = operations.openImportDialog()
      .importFileAndPreview(qifPath);
    importDialog.openBankSelection()
      .checkCountry("Switzerland")
      .checkContainsBanks("Credit Suisse")
      .checkBanksNotPresent("CIC", "BNP Paribas Fortis")
      .selectCountry("France")
      .checkContainsBanks("Boursorama", "CIC", "Société Générale")
      .checkBanksNotPresent("Credit Suisse")
      .validate();
    importDialog.close();

    accounts.createNewAccount()
      .setName("Account 2")
      .setAsSavings()
      .selectBank("France", "CIC")
      .setPosition(200.00)
      .validate();

    importDialog = operations.openImportDialog()
      .importFileAndPreview(qifPath);
    importDialog.openBankSelection()
      .checkCountry("All")
      .checkContainsBanks("CIC", "Credit Suisse", "BNP Paribas Fortis")
      .validate();
    importDialog.close();

    mainAccounts.openDelete("Account 1").validate();

    importDialog = operations.openImportDialog()
      .importFileAndPreview(qifPath);
    importDialog.openBankSelection()
      .checkCountry("France")
      .checkContainsBanks("Boursorama", "CIC", "Société Générale", "BNP Paribas Fortis")
      .validate();
    importDialog.close();

    savingsAccounts.openDelete("Account 2").validate();

    importDialog = operations.openImportDialog()
      .importFileAndPreview(qifPath);
    importDialog.openBankSelection()
      .checkCountry("All")
      .checkContainsBanks("CIC", "Credit Suisse", "BNP Paribas Fortis")
      .validate();
    importDialog.close();
  }
}
