package com.budgetview.functests.banks.synchro;

import com.budgetview.functests.checkers.ImportDialogChecker;
import com.budgetview.functests.checkers.OfxSynchoChecker;
import com.budgetview.functests.checkers.OtherBankSynchroChecker;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.functests.utils.QifBuilder;
import com.budgetview.io.importer.ofx.OfxConnection;
import com.budgetview.model.TransactionType;
import org.globsframework.model.Glob;
import org.globsframework.utils.Files;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SynchroTest extends LoggedInFunctionalTestCase {

  @Test
  public void testStandardImport() throws Exception {

    String path = QifBuilder
      .init(this)
      .addTransaction("2006/01/23", -1.1, "Menu K")
      .save();

    OtherBankSynchroChecker synchro = operations.openImportDialog().openSynchro("Other");
    synchro.createAccount("000123", "principal", "100.", path);
    synchro.doImportAndWaitForPreview()
      .checkAccount("principal")
      .setMainAccount()
      .completeImport();

    transactions.initContent()
      .add("23/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .check();
  }

  @Test
  public void testReImport() throws Exception {

    testStandardImport();

    String path = QifBuilder
      .init(this)
      .addTransaction("2006/01/24", -1.1, "Menu K")
      .save();

    OtherBankSynchroChecker synchro = importPanel.openImport().startSynchro();
    synchro.selectAccount(0)
      .setFile(path)
      .doImportAndWaitForPreview()
      .completeImport();

    transactions.initContent()
      .add("24/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .add("23/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .check();
  }

  @Test
  public void testNoImportFile() throws Exception {
    String path = QifBuilder.init(this)
      .addTransaction("2006/01/23", -1.10, "virement vers livret A")
      .save();
    ImportDialogChecker dialogChecker = operations.openImportDialog();

    OtherBankSynchroChecker synchro = dialogChecker.openSynchro("Other");
    synchro.createAccount("principal", "principal", "100.00", path);
    synchro.createAccount("secondary", "secondary", "10.00");
    synchro.createAccount("Livret A", "Livret A", "110.00");

    synchro.doImportAndWaitForPreview()
      .checkAccount("secondary")
      .setSavingsAccount()
      .importThisAccount()
      .checkAccount("Livret A")
      .setSavingsAccount()
      .importThisAccount()
      .checkAccount("principal")
      .setMainAccount()
      .completeImport();

    dialogChecker.checkClosed();

    mainAccounts.checkAccounts("principal");
    savingsAccounts.checkAccounts("secondary", "Livret A");
    savingsAccounts.checkAccount("secondary", 10.00, null);

    notifications.checkHidden();

    importPanel.openImport()
      .startSynchro()
      .selectAccount("secondary")
      .setAmount("100")
      .createAccount("Livret B", "Livret B", "110.00")
      .doImportAndWaitForCompletion()
      .complete();

    savingsAccounts.checkAccounts("secondary", "Livret A");
    savingsAccounts.checkAccount("secondary", 10.00, null);
    notifications.openDialog()
      .checkMessageCount(1)
      .checkMessage(0, "The last computed position for 'secondary' (10.00) is not the same as the " +
                       "imported one (100.00)")
      .close();

    views.selectCategorization();
    transactionCreation.show().selectAccount("secondary")
      .shouldUpdatePosition().setNotToBeReconciled().create(23, "new op", 90.00);

    savingsAccounts.checkPosition("secondary", 100.00);
    budgetView.transfer.createSavingSeries("To account Livret A", "principal", "Livret A");
    categorization.selectTransaction("virement vers livret A")
      .selectTransfers()
      .selectSeries("To account Livret A");
    transactions
      .initAmountContent()
      .add("23/01/2006", "VIREMENT VERS LIVRET A", -1.10, "To account Livret A", 100.00, 100.00, "principal")
      .add("23/01/2006", "NEW OP", 90.00, "To categorize", 100.00, 210.00, "secondary")
      .check();

    savingsAccounts.checkAccount("Livret A", 110.00, null);

    String path2 = QifBuilder
      .init(this)
      .addTransaction("2006/01/30", -2.20, "virement vers livret A")
      .save();

    importPanel.openImport().startSynchro()
      .selectAccount("principal")
      .setFile(path2)
      .setAmount("500")
      .doImportAndWaitForPreview()
      .doImport()
      .complete();

    timeline.selectAll();
    transactions.initAmountContent()
      .add("30/01/2006", "VIREMENT VERS LIVRET A", -2.20, "To account Livret A", 97.80, 97.80, "principal")
      .add("23/01/2006", "VIREMENT VERS LIVRET A", -1.10, "To account Livret A", 100.00, 100.00, "principal")
      .add("23/01/2006", "NEW OP", 90.00, "To categorize", 100.00, 210.00, "secondary")
      .check();

    mainAccounts.changePosition("principal", 500.00, "");
    savingsAccounts.checkAccount("Livret A", 110.00, null);

    setCurrentDate("2011/02/02");
    restartApplicationFromBackup();

    importPanel.openImport().startSynchro()
      .selectAccount("principal")
      .setFile(
        QifBuilder
          .init(this)
          .addTransaction("2011/02/01", -1000.00, "Autre vir. A") // pas d'auto categorization
          .save())
      .setAmount("1000")
      .selectAccount("Livret A")
      .setAmount("300")
      .doImportAndWaitForPreview()
      .doImport()
      .complete();

    categorization.selectTransaction("Autre vir. A")
      .selectTransfers()
      .selectSeries("To account Livret A");

    savingsAccounts.changePosition("Livret A", 300.00, "");

    mainAccounts.changePosition("principal", 1000.00, "");
    savingsAccounts.checkAccount("Livret A", 300.00, null);

    timeline.selectAll();
    transactions.initAmountContent()
      .add("01/02/2011", "AUTRE VIR. A", -1000.00, "To account Livret A", 1000.00, 1000.00, "principal")
      .add("30/01/2006", "VIREMENT VERS LIVRET A", -2.20, "To account Livret A", 2000.00, 2000.00, "principal")
      .add("23/01/2006", "VIREMENT VERS LIVRET A", -1.10, "To account Livret A", 2002.20, 2002.20, "principal")
      .add("23/01/2006", "NEW OP", 90.00, "To categorize", 100.00, 400.00, "secondary")
      .check();
  }

  @Test
  public void testImportOneOfxTakePositionInOfx() throws Exception {
    String path = OfxBuilder
      .init(this)
      .addBankAccount(30004, 12345, "000123", 100.00, "2006/01/23")
      .addTransaction("2006/01/23", -10.00, "Menu K")
      .save();

    OtherBankSynchroChecker synchro = operations.openImportDialog().openSynchro("Other");
    synchro.createAccount("000123 123", "principal", "", path);
    synchro.doImportAndWaitForPreview()
      .checkAccount("Account n. 000123")
      .checkAccountPosition(100.00)
      .setMainAccount()
      .completeImport();

    mainAccounts.checkPosition("Account n. 000123", 100.00);
  }

  @Test
  public void testImportTwoAccountsInSameOfxFile() throws Exception {

    String path = OfxBuilder
      .init(this)
      .addBankAccount(30004, 12345, "000123", 100.00, "2006/01/23")
      .addTransaction("2006/01/23", -10.00, "Menu K")
      .addBankAccount(30004, 12345, "000246", 200.00, "2006/01/23")
      .addTransaction("2006/01/23", -20.00, "FNAC")
      .save();

    OtherBankSynchroChecker synchro = operations.openImportDialog().openSynchro("Other");
    synchro.createAccount("000123 123", "principal", "", path);
    synchro.createAccount("000246 123", "secondaire", "", path);
    synchro.doImportAndWaitForPreview()
      .checkAccount("Account n. 000123")
      .checkAccountPosition(100.00)
      .setMainAccount()
      .doImport()
      .checkAccount("Account n. 000246")
      .checkAccountPosition(200.00)
      .setMainAccount()
      .completeImport();

    transactions.initContent()
      .add("23/01/2006", TransactionType.PRELEVEMENT, "FNAC", "", -20.00)
      .add("23/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -10.00)
      .check();
    mainAccounts.checkPosition("Account n. 000123", 100.);
    mainAccounts.checkPosition("Account n. 000246", 200.);
  }

  @Test
  public void testOfxDirectConnect() throws Exception {
    final String fileName = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "TX 1")
      .addTransaction("2006/01/11", -2.2, "TX 2")
      .save();

    final ArrayList<OfxConnection.AccountInfo> accountInfoArrayList = new ArrayList<OfxConnection.AccountInfo>();

    OfxConnection.register(new OfxConnection() {
                             public List<AccountInfo> getAccounts(String user, String password, String date, String url, String org, String fid, String uuid, final boolean v2) {
                               AccountInfo account1 = new AccountInfo(null, "1223", "any");
                               accountInfoArrayList.add(account1);
                               return accountInfoArrayList;
                             }

                             public String loadOperation(Glob realAccount, String fromDate, String user, String password, String url, String org, String fid, final String uuid, final boolean v2) throws IOException {
                               assertEquals("a", user);
                               assertEquals("b", password);
                               return Files.loadStreamToString(new FileInputStream(fileName), "UTF-8");
                             }
                           }
    );

    ImportDialogChecker dialogChecker = operations.openImportDialog();
    OfxSynchoChecker synchro = dialogChecker
      .selectBankForDownload("Bank One")
      .checkSecurityInfo("Secure connection")
      .openOfxSynchro();
    synchro.checkPasswordEmpty();
    synchro.enter("a", "b");
    synchro.doImportAndWaitForPreview()
      .setMainAccount()
      .setAccountName("compte principal")
      .doImport()
      .checkNoErrorMessage()
      .completeLastStep();

    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "TX 2", "", -2.20)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "TX 1", "", -1.10)
      .check();

    accountInfoArrayList.add(new OfxConnection.AccountInfo(null, "12345", "others"));

    operations.openImportDialog()
      .checkSynchroAvailableForAccounts("compte principal")
      .close();
  }

  @Test
  public void testManuallyImportingFilesDoesNotEnableSynchroButton() throws Exception {
    final String fileName = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "TX 1")
      .addTransaction("2006/01/11", -2.2, "TX 2")
      .save();
    operations.importQifFile(fileName, "Other", 100.);

    OtherBankSynchroChecker synchro = operations.openImportDialog().openSynchro("Other");
    synchro.createAccount("principal", "principal", "100.", fileName);
    synchro.doImportAndWaitForPreview()
      .selectAccount("Main account")
      .completeImport();
  }

  @Test
  public void testManagingConnectionErrors() throws Exception {
    String path = QifBuilder
      .init(this)
      .addTransaction("2006/01/23", -1.1, "Menu K")
      .save();

    OtherBankSynchroChecker synchro = operations.openImportDialog().openSynchro("Other");
    synchro
      .checkNoAccountDisplayed()
      .createAccount("000123", "principal", "100.", path);
    synchro.checkIdentificationFailedError()
      .checkTitle("Login failed")
      .checkMessageContains("The identifier and password you provided have been rejected")
      .close();
    synchro.checkPanelShown();
    synchro.checkConnectionException()
      .checkTitle("Download error")
      .checkMessageContains("A problem was found")
      .switchToDetails()
      .checkDetailsIntroContains("To help us fix this problem")
      .checkDetailsIntroContains("support@mybudgetview.com")
      .checkDetailsContain("bank: Autre")
      .checkDetailsContain("location: [current]")
      .checkDetailsContain("java.lang.RuntimeException: boom")
      .close();
    synchro
      .clearErrors()
      .selectAccount(0)
      .setFile(path)
      .doImportAndWaitForPreview()
      .checkAccount("principal")
      .setMainAccount()
      .completeImport();

    transactions.initContent()
      .add("23/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .check();
  }

  @Test
  public void testUsingSynchroAfterManualOfxDownload() throws Exception {

    OfxBuilder
      .init(this)
      .addBankAccount(30004, 12345, "000123", 100.00, "2005/12/20")
      .addTransaction("2005/12/20", -100.00, "Operation 1 compte 123")
      .addBankAccount(30004, 12345, "000345", 300.00, "2005/12/20")
      .addTransaction("2005/12/20", -300.00, "Operation 1 compte 345")
      .load();

    String path = OfxBuilder
      .init(this)
      .addBankAccount(30004, 12345, "000123", 90.00, "2006/01/23")
      .addTransaction("2006/01/23", -10.00, "Operation 2 compte 123")
      .addBankAccount(30004, 12345, "000246", 200.00, "2006/01/23")
      .addTransaction("2006/01/23", -20.00, "Operation 2 compte 246")
      .save();

    OtherBankSynchroChecker synchro = operations.openImportDialog().openSynchro("Other");
    synchro.createAccount("000123", "principal", "90.00", path);
    synchro.createAccount("000246", "secondaire", "200.00", path);
    synchro.doImportAndWaitForPreview()
      .checkExistingAccountDescription("Account n.000123 BNP Paribas Position: 100.00 on 2005/12/20")
      .doImport()
      .checkAccount("Account n. 000246")
      .checkAccountPosition(200.00)
      .setMainAccount()
      .completeImport();

    transactions.initContent()
      .add("23/01/2006", TransactionType.PRELEVEMENT, "OPERATION 2 COMPTE 246", "", -20.00)
      .add("23/01/2006", TransactionType.PRELEVEMENT, "OPERATION 2 COMPTE 123", "", -10.00)
      .check();

    operations.openImportDialog()
      .checkSynchroAvailableForAccounts("Account n. 000123", "Account n. 000246")
      .checkManualDownloadAvailableForAccounts("Account n. 000345")
      .checkManualDownloadLink("BNP Paribas", "http://www.bnpparibas.net")
      .close();
  }

  @Test
  public void testMultipleSynchroInSameBank() throws Exception {
    String path = QifBuilder
      .init(this)
      .addTransaction("2006/01/23", -10.00, "Menu K")
      .save();

    OtherBankSynchroChecker synchro = operations.openImportDialog().openSynchro("Other");
    synchro.setCode("1")
      .createAccount("000123 123", "principal", "", path)
      .setAmount("100");
    synchro.doImportAndWaitForPreview()
      .checkAccount("principal")
      .checkAccountPosition(100.00)
      .setMainAccount()
      .completeImport();

    mainAccounts.checkPosition("principal", 100.00);

    String path2 = QifBuilder
      .init(this)
      .addTransaction("2006/02/23", -10.00, "Menu K")
      .save();
    //  on change le code mais on garde le meme comptes
    OtherBankSynchroChecker secondSynchro = importPanel.openImport().startSynchro();
    secondSynchro.checkCode("1")
      .setCode("2")
      .selectAccount("principal")
      .setFile(path2)
      .doImportAndWaitForPreview()
      .completeImport();
    mainAccounts.checkPosition("principal", 90);

    OtherBankSynchroChecker thrirdSynchro = importPanel.openImport().startSynchro();
    thrirdSynchro.checkCode("2")
      .removeAccount("principal")
      .setCode("3")
      .createAccount("321", "nex account", null)
      .setFile(path2)
      .doImportAndWaitForPreview()
      .completeImport();
    mainAccounts.checkPosition("principal", 90);
  }

  @Test
  public void testClosedAccountsAreNotShown() throws Exception {
    String path = OfxBuilder
      .init(this)
      .addBankAccount(30004, 12345, "000123", 100.00, "2006/01/23")
      .addTransaction("2008/07/23", -10.00, "Menu K")
      .addBankAccount(30004, 12345, "000246", 200.00, "2006/01/23")
      .addTransaction("2008/08/23", -20.00, "FNAC")
      .save();

    operations.openImportDialog().openSynchro("Other")
      .createAccount("000123", "account1", "", path)
      .createAccount("000246", "account2", "", path)
      .doImportAndWaitForPreview()
      .checkAccount("Account n. 000123")
      .setMainAccount()
      .doImport()
      .checkAccount("Account n. 000246")
      .setMainAccount()
      .completeImport();

    operations.openImportDialog()
      .checkSynchroAvailableForAccounts("Account n. 000123", "Account n. 000246")
      .close();

    mainAccounts.edit("Account n. 000123")
      .setEndDate("2008/07/25")
      .validate();

    operations.openImportDialog()
      .checkSynchroAvailableForAccounts("Account n. 000246")
      .close();

    timeline.selectMonth(200807);
    mainAccounts.edit("Account n. 000123")
      .setEndDate("2008/09/30")
      .validate();

    timeline.selectMonth(200808);
    operations.openImportDialog()
      .checkSynchroAvailableForAccounts("Account n. 000123", "Account n. 000246")
      .close();

    mainAccounts.edit("Account n. 000246")
      .setEndDate("2008/08/23")
      .validate();

    operations.openImportDialog()
      .checkSynchroAvailableForAccounts("Account n. 000123")
      .close();
  }

  @Test
  public void testDeletedAccountsAreNotShown() throws Exception {
    String path = OfxBuilder
      .init(this)
      .addBankAccount(30004, 12345, "000123", 100.00, "2006/01/23")
      .addTransaction("2008/07/23", -10.00, "Menu K")
      .addBankAccount(30004, 12345, "000246", 200.00, "2006/01/23")
      .addTransaction("2008/08/23", -20.00, "FNAC")
      .save();

    operations.openImportDialog().openSynchro("Other")
      .createAccount("000123", "account1", "", path)
      .createAccount("000246", "account2", "", path)
      .doImportAndWaitForPreview()
      .checkAccount("Account n. 000123")
      .setMainAccount()
      .doImport()
      .checkAccount("Account n. 000246")
      .setMainAccount()
      .completeImport();

    operations.openImportDialog()
      .checkSynchroAvailableForAccounts("Account n. 000123", "Account n. 000246")
      .close();

    mainAccounts.openDelete("Account n. 000123").validate();

    operations.openImportDialog()
      .checkSynchroAvailableForAccounts("Account n. 000246")
      .close();

    timeline.selectMonth(200807);
    mainAccounts.openDelete("Account n. 000246").validate();

    operations.openImportDialog()
      .checkSynchroNotShown()
      .close();

    timeline.selectMonth(200808);
    operations.openImportDialog()
      .checkSynchroNotShown()
      .close();
  }
}