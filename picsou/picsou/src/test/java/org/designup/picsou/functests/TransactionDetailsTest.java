package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class TransactionDetailsTest extends LoggedInFunctionalTestCase {
  public void testLabel() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", 15.10, "Quick")
      .addTransaction("2008/06/16", 27.50, "Quick")
      .addTransaction("2008/06/15", 15.50, "McDo")
      .load();

    transactions.getTable().selectRow(0);
    transactionDetails.checkLabel("Quick");

    transactions.getTable().selectRows(0, 1);
    transactionDetails.checkLabel("Quick");

    transactions.getTable().selectRows(0, 2);
    transactionDetails.checkLabel("...");
  }

  public void testDate() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", 15.10, "Quick")
      .addTransaction("2008/06/18", 27.50, "Quick")
      .addTransaction("2008/06/15", 15.50, "McDo")
      .load();

    transactions.getTable().selectRow(0);
    transactionDetails.checkDate("18/06/2008");

    transactions.getTable().selectRows(0, 1);
    transactionDetails.checkDate("18/06/2008");

    transactions.getTable().selectRows(0, 2);
    transactionDetails.checkNoDate();

    transactions.getTable().clearSelection();
    transactionDetails.checkNoDate();
  }

  public void testAmount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/18", 10.00, "Quick")
      .addTransaction("2008/06/18", 30.00, "Quick")
      .addTransaction("2008/06/15", 20.00, "McDo")
      .load();

    transactions.getTable().selectRow(0);
    transactionDetails.checkAmount("Amount", "10.00");
    transactionDetails.checkNoAmountStatistics();

    transactions.getTable().selectRows(0, 1);
    transactionDetails.checkAmount("Total amount", "40.00");
    transactionDetails.checkAmountStatistics("10.00", "30.00", "20.00");

    transactions.getTable().clearSelection();
    transactionDetails.checkNoAmount();
    transactionDetails.checkNoAmountStatistics();
  }
}
