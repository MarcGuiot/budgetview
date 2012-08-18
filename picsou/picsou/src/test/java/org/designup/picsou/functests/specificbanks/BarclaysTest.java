package org.designup.picsou.functests.specificbanks;

import org.designup.picsou.model.TransactionType;

public class BarclaysTest extends SpecificBankTestCase{
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