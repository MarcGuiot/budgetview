package org.designup.picsou.functests.banks.synchro;

import org.designup.picsou.functests.checkers.ImportDialogChecker;
import org.designup.picsou.functests.checkers.OfxSynchoChecker;
import org.designup.picsou.functests.checkers.OtherBankSynchroChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.importer.ofx.OfxConnection;
import org.designup.picsou.model.TransactionType;
import org.globsframework.model.Glob;
import org.globsframework.utils.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SynchroTest extends LoggedInFunctionalTestCase {

  public void testStandardImport() throws Exception {

    String path = QifBuilder
      .init(this)
      .addTransaction("2006/01/23", -1.1, "Menu K")
      .save();

    importPanel.checkImportMessage("Import your operations");
    importPanel.checkSynchroButtonHidden();

    OtherBankSynchroChecker synchro = operations.openImportDialog().openSynchro("Other");
    synchro.createNew("princi", "princi", "100.", path);
    synchro.doImport()
      .setMainAccount()
      .completeImport();

    importPanel.checkImportMessage("Import other operations");
    importPanel.checkSynchroMessage("Download your accounts from Other");

    transactions.initContent()
      .add("23/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .check();
  }

  public void testReImport() throws Exception {

    testStandardImport();

    String path = QifBuilder
      .init(this)
      .addTransaction("2006/01/24", -1.1, "Menu K")
      .save();

    OtherBankSynchroChecker synchro = importPanel.openSynchro();
    synchro.select(0)
      .setFile(path);

    synchro.doImport()
      .completeImport();

    transactions.initContent()
      .add("24/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .add("23/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .check();
  }

  public void testNoImportFile() throws Exception {
    String path = QifBuilder
      .init(this)
      .addTransaction("2006/01/23", -1.1, "virement vers livret A")
      .save();
    ImportDialogChecker dialogChecker = operations.openImportDialog();

    OtherBankSynchroChecker synchro = dialogChecker.openSynchro("Other");
    synchro.createNew("princi", "princi", "100.", path);
    synchro.createNew("secondary", "secondary", "10.", null);
    synchro.createNew("Livret A", "Livret A", "110.", null);

    synchro.doImport()
      .checkAccount("secondary")
      .setSavingsAccount()
      .importThisAccount()
      .checkAccount("Livret A")
      .setSavingsAccount()
      .importThisAccount()
      .checkAccount("princi")
      .setMainAccount()
      .completeImport();

    mainAccounts.checkAccountNames("princi");
    savingsAccounts.checkAccountNames("secondary", "Livret A");

    savingsAccounts.checkAccount("secondary", 10, null);

    importPanel.openSynchro()
      .selectAccount("secondary")
      .setAmount("100")
      .createNew("Livret B", "Livret B", "110.", null)
      .doImport()
      .complete();

    savingsAccounts.checkAccountNames("secondary", "Livret A");
    savingsAccounts.checkAccount("secondary", 10, null);
    budgetViewMessageChecker.checkFirstImportMessage("secondary", 10., 100.);

    views.selectCategorization();
    transactionCreation.show().selectAccount("secondary")
      .shouldUpdatePosition().setNotToBeReconcile().create(23, "new op", 90);

    savingsAccounts.checkPosition("secondary", 100);

    categorization.selectTransaction("virement vers livret A")
      .selectSavings()
      .selectSeries("To account Livret A");
    transactions
      .initAmountContent()
      .add("23/01/2006", "VIREMENT VERS LIVRET A", -1.10, "To account Livret A", 100.00, 100.00, "princi")
      .add("23/01/2006", "VIREMENT VERS LIVRET A", 1.10, "To account Livret A", 111.10, 211.10, "Livret A")
      .add("23/01/2006", "NEW OP", 90.00, "To categorize", 100.00, 210.00, "secondary")
      .check();

    savingsAccounts.checkAccount("Livret A", 111.10, null);

    String path2 = QifBuilder
      .init(this)
      .addTransaction("2006/01/30", -2.2, "virement vers livret A")
      .save();

    importPanel.openSynchro()
      .selectAccount("princi")
      .setFile(path2)
      .setAmount("500")
      .doImport()
      .doImport()
      .complete();

    timeline.selectAll();
    transactions.initAmountContent()
      .add("30/01/2006", "VIREMENT VERS LIVRET A", -2.20, "To account Livret A", 97.80, 97.80, "princi")
      .add("30/01/2006", "VIREMENT VERS LIVRET A", 2.20, "To account Livret A", 113.30, 213.30, "Livret A")
      .add("23/01/2006", "VIREMENT VERS LIVRET A", -1.10, "To account Livret A", 100.00, 100.00, "princi")
      .add("23/01/2006", "VIREMENT VERS LIVRET A", 1.10, "To account Livret A", 111.10, 211.10, "Livret A")
      .add("23/01/2006", "NEW OP", 90.00, "To categorize", 100.00, 210.00, "secondary")
      .check();

    mainAccounts.changePosition("princi", 500., "");
    savingsAccounts.checkAccount("Livret A", 113.30, null);

    setCurrentDate("2011/02/02");
    restartApplicationFromBackup();

    importPanel.openSynchro()
      .selectAccount("princi")
      .setFile(
        QifBuilder
          .init(this)
          .addTransaction("2011/02/01", -1000, "Autre vir. A") // pas d'auto categorization
          .save())
      .setAmount("1000")
      .selectAccount("Livret A")
      .setAmount("300")
      .doImport()
      .doImport()
      .complete();

    categorization.selectTransaction("Autre vir. A")
      .selectSavings()
      .selectSeries("To account Livret A");

    savingsAccounts.changePosition("Livret A", 300., "");

    mainAccounts.changePosition("princi", 1000., "");
    savingsAccounts.checkAccount("Livret A", 300, null);

    timeline.selectAll();
    transactions.initAmountContent()
      .add("01/02/2011", "AUTRE VIR. A", -1000.00, "To account Livret A", 1000.00, 1000.00, "princi")
      .add("01/02/2011", "AUTRE VIR. A", 1000.00, "To account Livret A", 300.00, 400.00, "Livret A")
      .add("30/01/2006", "VIREMENT VERS LIVRET A", -2.20, "To account Livret A", 2000.00, 2000.00, "princi")
      .add("30/01/2006", "VIREMENT VERS LIVRET A", 2.20, "To account Livret A", -700.00, -600.00, "Livret A")
      .add("23/01/2006", "VIREMENT VERS LIVRET A", -1.10, "To account Livret A", 2002.20, 2002.20, "princi")
      .add("23/01/2006", "VIREMENT VERS LIVRET A", 1.10, "To account Livret A", -702.20, -602.20, "Livret A")
      .add("23/01/2006", "NEW OP", 90.00, "To categorize", 100.00, -603.30, "secondary")
      .check();
  }

  public void testOfx() throws Exception {
    final String fileName = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "TX 1")
      .addTransaction("2006/01/11", -2.2, "TX 2")
      .save();

    final ArrayList<OfxConnection.AccountInfo> accountInfoArrayList = new ArrayList<OfxConnection.AccountInfo>();

    OfxConnection.register(new OfxConnection() {
      public List<AccountInfo> getAccounts(String user, String password, String date, String url, String org, String fid) {
        AccountInfo account1 = new AccountInfo(null, "1223", "any");
        accountInfoArrayList.add(account1);
        return accountInfoArrayList;
      }

      public void loadOperation(Glob realAccount, String fromDate, String user, String password, String url, String org, String fid, File outputFile) throws IOException {
        assertEquals("a", user);
        assertEquals("b", password);
        Files.copyStream(new FileInputStream(fileName), new FileOutputStream(outputFile));
      }
    }
    );
    ImportDialogChecker dialogChecker = operations.openImportDialog();
    OfxSynchoChecker synchro = dialogChecker
      .selectBankForDownload("la banque postale")
      .checkSecurityInfo("Secure connection")
      .openOfxSynchro();
    synchro.checkPasswordEmpty();
    synchro.enter("a", "b");

    synchro.doImport()
      .setMainAccount()
      .setAccountName("compte principale")
      .doImport()
      .checkNoErrorMessage()
      .completeLastStep();

    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "TX 2", "", -2.20)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "TX 1", "", -1.10)
      .check();

    accountInfoArrayList.add(new OfxConnection.AccountInfo(null, "12345", "others"));

    importPanel.checkImportMessage("Import other operations");
    importPanel.checkSynchroMessage("Download your accounts from La Banque Postale");
  }

  public void testImportFileDoesNotEnableSynchroButton() throws Exception {
    final String fileName = QifBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "TX 1")
      .addTransaction("2006/01/11", -2.2, "TX 2")
      .save();
    operations.importQifFile(fileName, "Other", 100.);
    importPanel.checkSynchroNotVisible();
    ImportDialogChecker dialogChecker = operations.openImportDialog();
    OtherBankSynchroChecker checker = dialogChecker.openSynchro("Other");
    checker.createNew("princi", "princi", "100.", fileName);
    ImportDialogChecker importDialogChecker = checker.doImport();
    importDialogChecker
      .selectAccount("Main account")
      .completeImport();
    importPanel.checkSynchroVisible();
  }

}
