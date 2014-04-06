package org.designup.picsou.functests.transactions;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class TransactionPrintingTest extends LoggedInFunctionalTestCase {

  public void test() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("30002", 123, "00001123", 100.00, "2008/06/01")
      .addTransactionWithNote("2013/05/01", -70.00, "ESSO", "frais pro")
      .addTransactionWithNote("2013/05/03", -30.00, "PEAGE", "")
      .addTransaction("2013/05/04", "2013/05/06", -100.00, "MC DO")
      .addTransactionWithNote("2013/04/15", -300.00, "FNAC", "Hi fi")
      .addBankAccount("30002", 123, "00004567", 500.00, "2008/06/01")
      .addTransactionWithNote("2013/05/02", -200.00, "SOGEN", "")
      .load();

    categorization.setNewVariable("ESSO", "Essence");

    transactions.print();
    printer.getTransactions()
      .add("2013/05/01", "2013/05/01", "ESSO ", "-70.00 ", "Essence", "frais pro", "Account n. 00001123 : -270.00")
      .add("2013/05/02", "2013/05/02", "SOGEN", "-200.00", "", "", "Account n. 00004567 : 300.00")
      .add("2013/05/03", "2013/05/03", "PEAGE", "-30.00 ", "", "", "Account n. 00001123 : -300.00")
      .add("2013/05/04", "2013/05/06", "MC DO", "-100.00", "", "", "Account n. 00001123 : -400.00")
      .check();

    timeline.selectMonth(201304);
    transactions.print();
    printer.getTransactions()
      .add("2013/04/15", "2013/04/15", "FNAC", "-300.00", "", "Hi fi", "Account n. 00001123 : -200.00")
      .check();

    transactions.setSearchText("xx");
    transactions.checkEmpty();
    transactions.checkPrintDisabled();
  }

}
