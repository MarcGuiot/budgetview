package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class LaPosteTest extends SpecificBankTestCase {
  public void test() throws Exception {
    operations.importOfxFile(getFile("laposte1.ofx"));
    periods.selectCells(0, 1);
    transactions
      .initContent()
      .add("08/08/2006", TransactionType.CREDIT_CARD, "SANEMA", "", -5.00)
      .add("03/08/2006", TransactionType.CHECK, "CHEQUE N. 6557005", "", -56.14)
      .add("02/08/2006", TransactionType.WITHDRAWAL, "RETRAIT MASTERCA 09H59", "", -60.00)
      .add("02/08/2006", TransactionType.DEPOSIT, "REMISE CHEQUES", "", 292.82)
      .add("01/08/2006", TransactionType.CHECK, "CHEQUE N. 6557002", "", -11.00)
      .add("01/08/2006", TransactionType.CHECK, "CHEQUE N. 6557004", "", -60.00)
      .add("30/07/2006", TransactionType.CREDIT_CARD, "ATAC MAG GAILL", "", -11.35)
      .add("30/07/2006", TransactionType.CREDIT_CARD, "COINTIN ET CIE", "", -27.20)
      .add("29/07/2006", TransactionType.CREDIT_CARD, "CENTRE E LECLE", "", -61.85)
      .add("28/07/2006", TransactionType.CREDIT_CARD, "ST MARCEL DIST", "", -32.22)
      .check();
  }
}
