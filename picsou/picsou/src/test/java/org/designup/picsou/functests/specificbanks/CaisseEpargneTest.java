package org.designup.picsou.functests.specificbanks;

import org.designup.picsou.model.TransactionType;

public class CaisseEpargneTest extends SpecificBankTestCase {
  public void test() throws Exception {
    operations.importQifFile(getFile("caisse_epargne.qif"), "Caisse d'épargne", 0.);
    timeline.selectAll();
    views.selectData();
    transactions.getTable().getHeader().click(1);
    transactions.initContent()
      .add("05/09/2008", TransactionType.PRELEVEMENT, "AGF 12743YO43PH93175", "", -23.18)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "ECUREUIL ASSURANCES IARD *PJ 001919414", "", -4.00)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "ECUREUIL ASSURANCES IARD *HABITATION 001919425", "", -33.00)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "LYONNAISE DES EAUX FRANC A40 001 5 35 5 239300 00", "", -49.00)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "ECH PRET 0189650 DATE 20080905", "", -184.36)
      .add("05/09/2008", TransactionType.VIREMENT, "C.A.F DE L'ESSONNE P 7246098DANAME 082008ME", "", 1111.11)
      .add("05/09/2008", TransactionType.WITHDRAWAL, "RETRAIT CHABRIERES GIF080904", "", -20.00)
      .add("02/09/2008", "04/09/2008", TransactionType.CREDIT_CARD, "LIDL 1493", "", -56.00)
      .add("03/09/2008", TransactionType.CHECK, "CHEQUE N°5364043", "", -24.00)
      .add("02/09/2008", TransactionType.WITHDRAWAL, "RETRAIT CHABRIERES GIF080830", "", -30.00)
      .add("29/08/2008", TransactionType.VIREMENT, "THALES AIR DEFENCE 314 694 187181 010908681 681", "", 1234.56)
      .add("21/08/2008", TransactionType.WITHDRAWAL, "RETRAIT CHABRIERES GIF080820", "", -20.00)
      .add("20/08/2008", TransactionType.DEPOSIT, "REMISE N°1037870 DE 000003 CHEQUES", "", 123.12)
      .add("18/08/2008", "20/08/2008", TransactionType.CREDIT_CARD, "CHAMPION 7926MG", "", -123.21)
      .check();
  }

  public void testOfx() throws Exception {
    setCurrentDate("2010/03/24");
    operations.importOfxFile(getFile("caisse_epargne.ofx"), 100.);
    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("10/03/2010", TransactionType.PRELEVEMENT, "FREE TELECOM FREE HAUTDEBIT 199027559", "", -60.00)
      .add("09/03/2010", TransactionType.VIREMENT, "C.P.A.M. D'EVRY C.P.A.M. D&APOS;EVRY 100690007134", "", 10.82)
      .add("09/03/2010", TransactionType.PRELEVEMENT, "CB AMAZON EU SARL FACT 050310", "", -60.00)
      .add("08/03/2010", TransactionType.WITHDRAWAL, "RETRAIT DAB 0201080 080310 11H01", "", -11.35)
      .add("05/03/2010", TransactionType.VIREMENT, "C.A.F DE L'ESSONNE P 7246098DXXXXX 022010ME", "", 5.00)
      .add("05/03/2010", TransactionType.PRELEVEMENT, "ECH PRET 1321969 DU 05/03/10", "", -11.00)
      .add("02/03/2010", TransactionType.CHECK, "CHEQUE N°6579909", "", -56.14)
      .add("13/01/2010", TransactionType.CHECK, "CHEQUE N°6579929", "", -27.20)
      .check();

    operations.importOfxFile(getFile("caisse_epargne2.ofx"));

    views.selectHome();

    mainAccounts.checkAccount("Account n. 123123", 105.0, "2010/03/20");
  }
}
