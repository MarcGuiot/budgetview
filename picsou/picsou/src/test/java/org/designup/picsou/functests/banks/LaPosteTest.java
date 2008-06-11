package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class LaPosteTest extends SpecificBankTestCase {
  public void test() throws Exception {
    operations.importOfxFile(getFile("laposte1.ofx"));
    periods.selectCells(0, 1);
    transactions
      .initContent()
      .add("09/08/2006", TransactionType.CREDIT_CARD, "SANEMA                         08.08.06", "", -5.00)
      .add("03/08/2006", TransactionType.WITHDRAWAL, "CARTE MASTERCA 02/08/06 A 09H59", "", -60.00)
      .add("03/08/2006", TransactionType.CHECK, "6557005", "", -56.14)
      .add("03/08/2006", TransactionType.DEPOSIT, "REMISE DE CHEQUES DU 02/08/2006", "", 292.82)
      .add("01/08/2006", TransactionType.CHECK, "6557002", "", -11.00)
      .add("01/08/2006", TransactionType.CHECK, "6557004", "", -60.00)
      .add("31/07/2006", TransactionType.CREDIT_CARD, "ATAC MAG GAILL 30.07.06", "", -11.35)
      .add("31/07/2006", TransactionType.CREDIT_CARD, "CENTRE E LECLE 29.07.06", "", -61.85)
      .add("31/07/2006", TransactionType.CREDIT_CARD, "COINTIN ET CIE 30.07.06", "", -27.20)
      .add("31/07/2006", TransactionType.CREDIT_CARD, "ST MARCEL DIST 28.07.06", "", -32.22)
      .check();
  }
}
