package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.AccountEditionChecker;
import org.designup.picsou.functests.checkers.BankDownloadChecker;
import org.designup.picsou.functests.checkers.ImportDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.TransactionType;

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

  public void testCanAddBankFromImportDialog() throws Exception {

    /** Create bank and load first file **/

    String ofxFilePath = OfxBuilder.init(this)
      .addBankAccount(777777, 7777, "0001234", 1000.00, "2008/06/16")
      .addTransaction("2008/06/16", -27.50, "Burger King")
      .save();

    ImportDialogChecker importDialog = operations.openImportDialog();

    BankDownloadChecker bankDownload = importDialog.getBankDownload();

    bankDownload
      .addNewBank()
      .setName("")
      .checkValidationError("You must enter a name for this bank")
      .setName("TestBank")
      .checkNoErrorDisplayed()
      .setUrl("http://www.testbank.net")
      .validate();

    bankDownload
      .checkContainsBanks("TestBank", "BNP Paribas")
      .checkSelectedBank("TestBank");

    importDialog
      .setFilePath(ofxFilePath)
      .acceptFile()
      .setAccountName("TestAccount")
      .selectBank("TestBank")
      .setMainAccount()
      .checkFileContent(new Object[][]{
        {"2008/06/16", "Burger King", "-27.50"}
      })
      .completeImport();

    mainAccounts.checkAccount("TestAccount", 1000.00, "2008/06/16");
    mainAccounts.checkAccountWebsite("TestAccount", "TestBank", "http://www.testbank.net");

    /** Restart **/

    restartApplication();

    mainAccounts.checkAccount("TestAccount", 1000.00, "2008/06/16");
    mainAccounts.checkAccountWebsite("TestAccount", "TestBank", "http://www.testbank.net");

    /** Load second file **/

    String secondOfxFilePath = OfxBuilder.init(this)
      .addBankAccount(777777, 7777, "0001234", 970.00, "2008/07/16")
      .addTransaction("2008/07/16", -30.00, "Burger King")
      .save();

    ImportDialogChecker secondImportDialog = operations.openImportDialog();

    secondImportDialog.getBankDownload()
      .checkContainsBanks("TestBank");

    secondImportDialog
      .setFilePath(secondOfxFilePath)
      .acceptFile()
      .checkSelectedAccount("TestAccount")
      .completeImport();
  }

  public void testEditingUserBanksFromImportDialog() throws Exception {

    String ofxFilePath = OfxBuilder.init(this)
      .addBankAccount(777777, 7777, "0001234", 1000.00, "2008/06/16")
      .addTransaction("2008/06/16", -27.50, "Burger King")
      .save();

    ImportDialogChecker importDialog = operations.openImportDialog();

    BankDownloadChecker bankDownload = importDialog.getBankDownload();

    /** ----- Create the bank ----- */

    bankDownload
      .addNewBank()
      .checkTitle("Add a bank")
      .checkName("")
      .checkUrl("")
      .setName("TestBank")
      .setUrlAndValidate("http://www.testbank.net");

    bankDownload
      .checkSelectedBank("TestBank")
      .selectBank("CIC")
      .checkEditDisabled()
      .selectBank("TestBank");

    /** ----- Change the bank ----- */

    bankDownload.edit()
      .checkTitle("Edit bank")
      .checkName("TestBank")
      .checkUrl("http://www.testbank.net")
      .setName("NewBank")
      .setUrlAndValidate("http://www.newbank.net");

    bankDownload
      .checkBankNotPresent("TestBank")
      .checkContainsBanks("NewBank", "CIC")
      .checkSelectedBank("NewBank")
      .selectManualDownload()
      .checkManualDownloadHelp("NewBank", "http://www.newbank.net");

    importDialog
      .setFilePath(ofxFilePath)
      .acceptFile()
      .setAccountName("TestAccount")
      .selectBank("NewBank")
      .setMainAccount()
      .checkFileContent(new Object[][]{
        {"2008/06/16", "Burger King", "-27.50"}
      })
      .completeImport();

    mainAccounts.checkAccountWebsite("TestAccount", "NewBank", "http://www.newbank.net");

    /** ----- Try to delete the bank when it is used ----- */

    ImportDialogChecker secondImportDialog = operations.openImportDialog();

    secondImportDialog.getBankDownload()
      .selectBank("CIC")
      .checkDeleteDisabled()
      .selectBank("NewBank")
      .checkDeleteRejected("Delete bank", "This bank is used by account TestAccount. You cannot delete it.");

    secondImportDialog.close();

    mainAccounts.edit("TestAccount")
      .selectBank("CIC")
      .validate();

    /** ----- Delete the bank ----- */

    ImportDialogChecker thirdImportDialog = operations.openImportDialog();
    thirdImportDialog.getBankDownload()
      .selectBank("NewBank")
      .deleteAndCancel()
      .checkContainsBanks("NewBank")
      .delete("Delete bank", "This bank is not used. Do you want to delete it?")
      .checkBankNotPresent("NewBank");
    thirdImportDialog
      .setFilePath(OfxBuilder.init(this)
                     .addBankAccount(777777, 7777, "0001234", 1000.00, "2008/06/18")
                     .addTransaction("2008/06/18", -15.00, "Mc Do")
                     .save())
      .acceptFile()
      .completeImport();

    mainAccounts.edit("TestAccount")
      .checkSelectedBank("CIC")
      .checkBankNotPresentInList("NewBank")
      .validate();

    mainAccounts.checkAccountWebsite("TestAccount", "CIC", "http://www.cic.fr");
  }

  public void testEditingBanksFromAccountView() throws Exception {

    mainAccounts.createNewAccount()
      .setName("TestAccount")
      .selectNewBank("TestBank", "http://www.testbank.net")
      .setPosition(100.00)
      .validate();

    mainAccounts.createNewAccount()
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

    /** ------ Try to delete the bank while it is used by another account -- */

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

  public void testCancelEditionAndDeletion() throws Exception {
    mainAccounts.createNewAccount()
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

  public void testCreatingABankInFileImportPreview() throws Exception {
    String qifPath = QifBuilder.init(this)
      .addTransaction("2008/08/30", -50.00, "MacDo")
      .save();

    operations.openImportDialog()
      .setFilePath(qifPath)
      .acceptFile()
      .setAccountName("NewAccount")
      .addNewAccountBank("NewBank", "http://www.newbank.net")
      .setMainAccount()
      .checkFileContent(new Object[][]{
        {"2008/08/30", "MacDo", "-50.00"},
      })
      .completeImport();

    mainAccounts.checkAccountWebsite("NewAccount", "NewBank", "http://www.newbank.net");

    transactions.initContent()
      .add("30/08/2008", TransactionType.PRELEVEMENT, "MACDO", "", -50.00)
      .check();
  }
}
