package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class LaPosteTest extends SpecificBankTestCase {
  public void test() throws Exception {
    operations.importOfxFile(getFile("laposte1.ofx"));
    timeline.selectMonths("2006/07", "2006/08");
    transactions.getTable().getHeader().click(1);
    transactions
      .initContent()
      .add("08/08/2006", "09/08/2006", TransactionType.CREDIT_CARD, "SANEMA", "", -5.00)
      .add("02/08/2006", "03/08/2006", TransactionType.DEPOSIT, "REMISE CHEQUES", "", 292.82)
      .add("02/08/2006", "03/08/2006", TransactionType.WITHDRAWAL, "RETRAIT MASTERCA 09H59", "", -60.00)
      .add("03/08/2006", "03/08/2006", TransactionType.CHECK, "CHEQUE N. 6557005", "", -56.14)
      .add("01/08/2006", "01/08/2006", TransactionType.CHECK, "CHEQUE N. 6557002", "", -11.00)
      .add("01/08/2006", "01/08/2006", TransactionType.CHECK, "CHEQUE N. 6557004", "", -60.00)
      .add("30/07/2006", "31/07/2006", TransactionType.CREDIT_CARD, "ATAC MAG GAILL", "", -11.35)
      .add("30/07/2006", "31/07/2006", TransactionType.CREDIT_CARD, "COINTIN ET CIE", "", -27.20)
      .add("28/07/2006", "31/07/2006", TransactionType.CREDIT_CARD, "ST MARCEL DIST", "", -32.22)
      .add("29/07/2006", "31/07/2006", TransactionType.CREDIT_CARD, "CENTRE E LECLE", "", -61.85)
      .check();
  }

  public void test2() throws Exception {
    operations.importOfxFile(getFile("laposte2.ofx"));
    timeline.selectMonths("2008/09", "2008/10", "2008/11", "2008/12");
    transactions.getTable().getHeader().click(1);
    transactions
      .initContent()
      .add("23/12/2008", "24/12/2008", TransactionType.CREDIT_CARD, "MAG SUPER U", "", -64.87)
      .add("23/12/2008", TransactionType.CHECK, "CHEQUE N. 8582011", "", -15.00)
      .add("23/12/2008", TransactionType.VIREMENT, "RGF MFP", "", 7.50)
      .add("23/12/2008", TransactionType.DEPOSIT, "REMISE CHEQUES", "", 12.50)
      .add("22/12/2008", TransactionType.VIREMENT, "XXX         .", "", 1688.86)
      .add("15/12/2008", "16/12/2008", TransactionType.CREDIT_CARD, "FREE", "", -36.16)
      .add("15/12/2008", TransactionType.PRELEVEMENT, "PRELEVEMENT SFR", "", -49.39)
      .add("12/12/2008", "15/12/2008", TransactionType.WITHDRAWAL, "RETRAIT VISA 14H11", "", -80.00)
      .add("12/12/2008", TransactionType.PRELEVEMENT, "COFINOGA", "", -13.00)
      .add("10/12/2008", TransactionType.PRELEVEMENT, "FRAIS DE VIREMENT", "", -0.98)
      .add("10/12/2008", TransactionType.PRELEVEMENT, "VIREMENT PERMANENT", "", -820.00)
      .add("24/11/2008", TransactionType.VIREMENT, "VIREMENT", "", 210.00)
      .add("20/11/2008", TransactionType.PRELEVEMENT, "???", "", -10.00)
      .add("10/11/2008", TransactionType.DEPOSIT, "REMISE CHEQUES", "", 69.00)
      .add("21/10/2008", TransactionType.PRELEVEMENT, "FRAIS POUR PRELEVEMENT IMPAYE", "", -10.00)
      .add("03/10/2008", TransactionType.VIREMENT, "REMISE COMMERCIALE D AGIOS", "", 2.21)
      .add("03/10/2008", TransactionType.PRELEVEMENT, "INTERETS DEBITEURS 3ME TRIMESTRE", "", -2.21)
      .add("02/10/2008", TransactionType.PRELEVEMENT, "COTISATION TRIMESTRIELLE", "", -16.15)
      .add("01/10/2008", TransactionType.PRELEVEMENT, "VIREMENT PERMANENT", "", -50.00)
      .add("30/09/2008", "01/10/2008", TransactionType.CREDIT_CARD, "ALINEA 02", "", -9.80)
      .check();
  }

}
