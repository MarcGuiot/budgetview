package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class CICTest extends SpecificBankTestCase {
  public void test() throws Exception {
    operations.importOfxWithDeferred(getFile("cic1.ofx"), "Card n. 4976-0500-1234-4321");
    timeline.selectMonths("2006/05", "2006/06", "2006/07");

    views.selectData();
    transactions.getTable().getHeader().click(1);
    transactions
      .initContent()
      .add("08/07/2006", "08/07/2006", TransactionType.CREDIT_CARD, "EVEIL - JEUX ANTONY", "", -49.63)
      .add("22/06/2006", "22/06/2006", TransactionType.CREDIT_CARD, "MONOPRIX SCEAUX", "", -51.28)
      .add("15/06/2006", "15/06/2006", TransactionType.CREDIT, "PRET CAP+IN 10674 101147 06", "", -343.31)
      .add("01/06/2006", "01/06/2006", TransactionType.BANK_FEES, "FILBANQUE DONT TVA 0,54E IDT : 10", "", -3.30)
      .add("01/06/2006", "01/06/2006", TransactionType.BANK_FEES, "COMMISSIONS MAI 2006", "", -84.00)
      .add("31/05/2006", "31/05/2006", TransactionType.PRELEVEMENT, "RELEVE CARTE 04961018", "", -2172.00)
      .add("29/05/2006", "29/05/2006", TransactionType.PRELEVEMENT, "TIP FRANCE TELECOM MASSY NOR 107", "", -72.90)
      .add("18/05/2006", "18/05/2006", TransactionType.DEPOSIT, "REMISE CHEQUES REF10674R04", "", 400.00)
      .add("18/05/2006", "18/05/2006", TransactionType.VIREMENT, "ASS.GENERALES DE FRANCE AGFS", "", 6.00)
      .add("18/05/2006", "18/05/2006", TransactionType.WITHDRAWAL, "RETRAIT REF10674A01 CAR", "", -20.00)
      .add("17/05/2006", "17/05/2006", TransactionType.CHECK, "CHEQUE N°0366943", "", -63.00)
      .check();
  }
}
