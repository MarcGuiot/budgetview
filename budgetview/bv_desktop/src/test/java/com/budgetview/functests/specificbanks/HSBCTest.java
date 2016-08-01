package com.budgetview.functests.specificbanks;

import com.budgetview.model.TransactionType;
import org.junit.Test;

public class HSBCTest extends SpecificBankTestCase {
  @Test
  public void test() throws Exception {
    operations.importOfxFile(getFile("hsbc.ofx"));
    timeline.selectMonths("2006/05", "2006/06", "2006/07");
    views.selectData();
    transactions.getTable().getHeader().click(1);
    transactions
      .initContent()
      .add("17/05/2006", TransactionType.PRELEVEMENT, "CHEQUE 0366943", "", -63.00)
      .check();
  }
}