package org.designup.picsou.functests.banks;

import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class CICTest extends SpecificBankTestCase {
  public void test() throws Exception {
    operations.importOfxFile(getFile("cic1.ofx"));
    periods.selectCells(0, 1, 2);
    transactions
      .initContent()
      .add("08/07/2006", TransactionType.CREDIT_CARD, "EVEIL - JEUX ANTONY", "", -49.63)
      .add("22/06/2006", TransactionType.CREDIT_CARD, "MONOPRIX SCEAUX", "", -51.28)
      .add("15/06/2006", TransactionType.CREDIT, "CAP+IN 10674 101147 06", "", -343.31)
      .add("01/06/2006", TransactionType.BANK_FEES, "ABON FBQ DONT TVA 0,54E IDT : 10", "", -3.30, MasterCategory.BANK)
      .add("01/06/2006", TransactionType.BANK_FEES, "F COM INTERVENTION MAI 2006", "", -84.00, MasterCategory.BANK)
      .add("31/05/2006", TransactionType.PRELEVEMENT, "RELEVE CARTE 04961018", "", -2172.00)
      .add("29/05/2006", TransactionType.PRELEVEMENT, "TIP FRANCE TELECOM MASSY NOR 107", "", -72.90)
      .add("18/05/2006", TransactionType.DEPOSIT, "REM CHQ REF10674R04", "", 400.00)
      .add("18/05/2006", TransactionType.WITHDRAWAL, "REF10674A01 CAR", "", -20.00)
      .add("18/05/2006", TransactionType.VIREMENT, "ASS.GENERALES DE FRANCE AGFS", "", 6.00)
      .add("17/05/2006", TransactionType.CHECK, "0366943", "", -63.00)
      .check();
  }
}
