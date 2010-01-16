package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class INGDirectTest extends SpecificBankTestCase {
  public void test() throws Exception {
    operations.importQifFile(getFile("ingdirect.qif"), "ING Direct", 0.);
    timeline.selectAll();

    transactions.getTable().getHeader().click(1);
    transactions.initContent()
      .add("10/12/2008", TransactionType.VIREMENT, "PRÉLÈVEMENT AUTOMATIQUE GASTON", "", 100.00)
      .add("01/12/2008", TransactionType.PRELEVEMENT, "VIREMENTS LIBRES - GENERALI M. R.R.", "", -100.00)
      .add("01/12/2008", TransactionType.PRELEVEMENT, "VIREMENT VERS M. R.R VIRT DECEMBRE 1", "", -100.00)
      .add("10/11/2008", TransactionType.VIREMENT, "PRÉLÈVEMENT AUTOMATIQUE", "", 100.00)
      .add("26/03/2008", TransactionType.VIREMENT, "VIREMENT RECU GENEREUX BIENFAITEUR VIRT ING", "", 1000.00)
      .check();
  }
}
