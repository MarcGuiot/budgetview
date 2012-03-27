package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.BankDownloadChecker;
import org.designup.picsou.functests.checkers.ImportDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class BankEditionTest extends LoggedInFunctionalTestCase {

// TODO: a retablir pour le restartApplication()
//  protected void setUp() throws Exception {
//    resetWindow();
//    setCurrentDate("2008/08/30");
//    setInMemory(false);
//    setDeleteLocalPrevayler(true);
//    super.setUp();
//    setDeleteLocalPrevayler(false);
//  }

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

    fail("TODO: Retablir le setUp et l'enregistrement des serializers dans SerializationManager");
    
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
}
