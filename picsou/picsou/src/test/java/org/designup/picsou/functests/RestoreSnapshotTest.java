package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class RestoreSnapshotTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2008/08/30");
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    resetWindow();
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  public void testRestorePrevious() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/10", -400.0, "Auchan")
      .load();
    restartApplication();
    OfxBuilder.init(this)
      .addTransaction("2008/08/27", 1000, "Company")
      .load();

    operations.restoreSnapshot()
      .checkAvailableSnapshots(1)
      .restore(0);

    transactions.initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "COMPANY", "", 1000.00)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -400.00)
      .check();

    OfxBuilder.init(this)
      .addTransaction("2008/08/27", 500, "Company")
      .load();
    operations.setPasswordForAnonymous()
      .setPassword("user1", "pass1")
      .validate();

    OfxBuilder.init(this)
      .addTransaction("2008/08/27", -100, "Auchan")
      .load();

    restartApplication("user1", "pass1");

    OfxBuilder.init(this)
      .addTransaction("2008/08/27", -100, "ED")
      .load();

    operations.restoreSnapshot()
      .checkAvailableSnapshots(2)
      .restore(0);

    transactions.initContent()
      .add("27/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .add("27/08/2008", TransactionType.VIREMENT, "COMPANY", "", 500.00)
      .add("26/08/2008", TransactionType.VIREMENT, "COMPANY", "", 1000.00)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -400.00)
      .check();


    operations.restoreSnapshot()
      .checkAvailableSnapshots(3)
      .restoreWithCancel(2)
      .close();

    transactions.initContent()
      .add("27/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .add("27/08/2008", TransactionType.VIREMENT, "COMPANY", "", 500.00)
      .add("26/08/2008", TransactionType.VIREMENT, "COMPANY", "", 1000.00)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -400.00)
      .check();

    operations.restoreSnapshot()
      .checkAvailableSnapshots(3)
      .restore(2);

    transactions.initContent()
      .add("27/08/2008", TransactionType.VIREMENT, "COMPANY", "", 500.00)
      .add("26/08/2008", TransactionType.VIREMENT, "COMPANY", "", 1000.00)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -400.00)
      .check();
  }
}
