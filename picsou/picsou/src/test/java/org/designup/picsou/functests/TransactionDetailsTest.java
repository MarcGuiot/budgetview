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
}
