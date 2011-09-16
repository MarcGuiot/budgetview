package org.designup.picsou.functests.banks.synchro;

import org.designup.picsou.functests.checkers.ImportDialogChecker;
import org.designup.picsou.functests.checkers.OfxSynchoChecker;
import org.designup.picsou.functests.checkers.OtherBankSynchoChecker;
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

public class SynchoTest extends LoggedInFunctionalTestCase {

  public void testStandardImport() throws Exception {

    String path = QifBuilder
      .init(this)
      .addTransaction("2006/01/23", -1.1, "Menu K")
      .save();
    ImportDialogChecker dialogChecker = operations.openImportDialog();
    OtherBankSynchoChecker checker = dialogChecker.openSynchro("Autre");
    checker.createNew("princi", "princi", "100.", path);
    ImportDialogChecker importDialogChecker = checker.doImport();
    importDialogChecker
      .setMainAccount()
      .completeImport();

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

    OtherBankSynchoChecker bankSyncho = importPanel.openSynchro();
    bankSyncho.select(0)
      .setFile(path);
    ImportDialogChecker importDialogChecker = bankSyncho.doImport();
    importDialogChecker
      .setMainAccount()
      .completeImport();

    transactions.initContent()
      .add("24/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .add("23/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .check();
  }

  public void testNoImportFile() throws Exception {
    String path = QifBuilder
      .init(this)
      .addTransaction("2006/01/23", -1.1, "Menu K")
      .save();
    ImportDialogChecker dialogChecker = operations.openImportDialog();
    OtherBankSynchoChecker checker = dialogChecker.openSynchro("Autre");
    checker.createNew("princi", "princi", "100.", path);
    checker.createNew("secondary", "secondary", "10.", null);
    checker.createNew("Livret A", "Livret A", "110.", null);
    ImportDialogChecker importDialogChecker = checker.doImport();
    importDialogChecker
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

    OtherBankSynchoChecker synchoChecker = importPanel
      .openSynchro();
    synchoChecker
      .select("secondary")
      .setAmount("100");
    synchoChecker
      .createNew("Livret B", "Livret B", "110.", null)
      .doImport();

    savingsAccounts.checkAccountNames("secondary", "Livret A");
    savingsAccounts.checkAccount("secondary", 100, null);
  }


  public void testOfx() throws Exception {
    final String fileName = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "TX 1")
      .addTransaction("2006/01/11", -2.2, "TX 2")
      .save();

    final ArrayList<OfxConnection.AccountInfo> accountInfoArrayList = new ArrayList<OfxConnection.AccountInfo>();

    OfxConnection.register(new OfxConnection() {
      public List<AccountInfo> getAccounts(String user, String password, String url, String org, String fid) {
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
    OfxSynchoChecker checker = dialogChecker.openOfxSynchro("la banque postale");
    checker.checkPasswordEmpty();
    checker.enter("a", "b");
    ImportDialogChecker importDialogChecker = checker.doImport();
    importDialogChecker.setMainAccount()
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
  }
}
