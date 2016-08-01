package com.budgetview.functests.specificbanks;

import com.budgetview.model.TransactionType;
import org.junit.Test;

public class BarclaysTest extends SpecificBankTestCase {
  @Test
  public void test() throws Exception {
    operations.importOfxFile(getFile("barclays.ofx"), 0);
    timeline.selectAll();

    views.selectData();
    transactions.getTable().getHeader().click(1);
    transactions.initContent()
      .add("13/02/2009", TransactionType.PRELEVEMENT, "0295314 ACHAT ETC...", "", -100.00)
      .check();
  }
}
