package org.designup.picsou.functests.banks.synchro;

import org.designup.picsou.functests.checkers.ImportDialogChecker;
import org.designup.picsou.functests.checkers.OtherBankSynchoChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.TransactionType;

public class SynchoTest extends LoggedInFunctionalTestCase {

  public void testStandardImport() throws Exception {

    String path = QifBuilder
      .init(this)
      .addTransaction("2006/01/23", -1.1, "Menu K")
      .save();
    ImportDialogChecker dialogChecker = operations.openImportDialog();
    OtherBankSynchoChecker checker = dialogChecker.openSynchro("Autre");
    checker.createNew("princi", "princi", "100.", path);
    checker.next();
    checker.select("princi");
    ImportDialogChecker importDialogChecker = checker.doImport();
    importDialogChecker
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
    bankSyncho.next();
    ImportDialogChecker importDialogChecker = bankSyncho.doImport();
    importDialogChecker
      .completeImport();

    transactions.initContent()
      .add("24/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .add("23/01/2006", TransactionType.PRELEVEMENT, "MENU K", "", -1.10)
      .check();
  }
}
