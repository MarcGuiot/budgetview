package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class SGTest extends SpecificBankTestCase {

  public void test1() throws Exception {
    operations.importQifFile(100.0, getFile("sg1.qif"), "Societe Generale");
    transactions
      .initContent()
      .add("20/04/2006", TransactionType.CREDIT_CARD, "BISTROT ANDRE CARTE 06348905 PAIEMENT CB 1904 015 PARIS", "", -49.00)
      .add("20/04/2006", TransactionType.CREDIT_CARD, "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", "", -17.65)
      .add("19/04/2006", TransactionType.CREDIT_CARD, "SARL KALISTEA CARTE 06348905 PAIEMENT CB 1404 PARIS", "", -14.50)
      .add("13/04/2006", TransactionType.CREDIT_CARD, "STATION BP MAIL CARTE 06348905 PAIEMENT CB 1104 PARIS", "", -18.70)
      .check();
  }

  public void test2() throws Exception {
    operations.importQifFile(100.0, getFile("sg2.qif"), "Societe Generale");
    transactions
      .initContent()
      .add("22/07/2006", TransactionType.CREDIT_CARD, "ANTONYCARBURANT", "", -45.83)
      .add("21/07/2006", TransactionType.WITHDRAWAL, "12H37 PARIS LAFFITTE         000089", "", -60.00)
      .add("21/07/2006", TransactionType.VIREMENT, "VIR.LOGITEL", "", 1000.00)
      .add("21/07/2006", TransactionType.VIREMENT, "SG 04042 CPT 00050741769", "", -1000.00)
      .check();
  }
}
