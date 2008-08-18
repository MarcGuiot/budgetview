package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class SGTest extends SpecificBankTestCase {

  public void test1() throws Exception {
    operations.importQifFile(100.0, getFile("sg1.qif"), "Societe Generale");
    timeline.selectAll();
    transactions
      .initContent()
      .add("22/04/2006", TransactionType.CREDIT_CARD, "SACLAY", "", -55.49)
      .add("20/04/2006", TransactionType.CREDIT_CARD, "BISTROT ANDRE CARTE 06348905 PAIEMENT CB 1904 015 PARIS", "", -49.00)
      .add("20/04/2006", TransactionType.CREDIT_CARD, "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", "", -17.65)
      .add("19/04/2006", TransactionType.CREDIT_CARD, "SARL KALISTEA CARTE 06348905 PAIEMENT CB 1404 PARIS", "", -14.50)
      .add("13/04/2006", TransactionType.CREDIT_CARD, "STATION BP MAIL CARTE 06348905 PAIEMENT CB 1104 PARIS", "", -18.70)
      .check();
  }

  public void test2() throws Exception {
    operations.importQifFile(100.0, getFile("sg2.qif"), "Societe Generale");
    timeline.selectAll();
    transactions
      .initContent()
      .add("22/07/2006", TransactionType.CREDIT_CARD, "ANTONYCARBURANT", "", -45.83)
      .add("21/07/2006", TransactionType.WITHDRAWAL, "RETRAIT 12H37 PARIS LAFFITTE         000089", "", -60.00)
      .add("21/07/2006", TransactionType.VIREMENT, "VIREMENT LOGITEL", "", 1000.0)
      .add("21/07/2006", TransactionType.VIREMENT, "21.07 SG 04042 CPT 00050741769", "", -1000.00)
      .add("28/06/2006", TransactionType.CHECK, "CHEQUE N. 186", "", -206.04)
      .add("21/06/2006", TransactionType.VIREMENT, "", "", 10333.44)
      .add("18/06/2006", TransactionType.WITHDRAWAL, "RETRAIT 20H15 ANTONY A PAJEAUD       000009", "", -60.00)
      .add("05/06/2006", TransactionType.PRELEVEMENT, "TPS FRA01107365A040606/T.P.S. 000103017914", "", -34.50)
      .add("27/05/2006", TransactionType.WITHDRAWAL, "RETRAIT 15H49 ETRETAT                35179000", "", -70.00)
      .add("03/05/2006", TransactionType.WITHDRAWAL, "RETRAIT 21H02 MONTLHERY              00904963", "", -50.00)
      .add("24/04/2006", TransactionType.CREDIT_CARD, "SARL KALISTEA", "", -100.00)
      .check();
  }
}
