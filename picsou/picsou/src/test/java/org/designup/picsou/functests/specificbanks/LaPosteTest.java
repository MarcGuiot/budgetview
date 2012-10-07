package org.designup.picsou.functests.specificbanks;

import org.designup.picsou.model.TransactionType;

public class LaPosteTest extends SpecificBankTestCase {
  public void test() throws Exception {
    operations.importOfxFile(getFile("laposte1.ofx"));
    timeline.selectMonths("2006/07", "2006/08");
    views.selectData();
    transactions.getTable().getHeader().click(1);
    transactions
      .initContent()
      .add("08/08/2006", "09/08/2006", TransactionType.CREDIT_CARD, "SANEMA", "", -5.00)
      .add("02/08/2006", "03/08/2006", TransactionType.DEPOSIT, "REMISE CHEQUES", "", 292.82)
      .add("02/08/2006", "03/08/2006", TransactionType.WITHDRAWAL, "RETRAIT MASTERCA 09H59", "", -60.00)
      .add("03/08/2006", "03/08/2006", TransactionType.CHECK, "CHEQUE N°6557005", "", -56.14)
      .add("01/08/2006", "01/08/2006", TransactionType.CHECK, "CHEQUE N°6557002", "", -11.00)
      .add("01/08/2006", "01/08/2006", TransactionType.CHECK, "CHEQUE N°6557004", "", -60.00)
      .add("30/07/2006", "31/07/2006", TransactionType.CREDIT_CARD, "ATAC MAG GAILL", "", -11.35)
      .add("30/07/2006", "31/07/2006", TransactionType.CREDIT_CARD, "COINTIN ET CIE", "", -27.20)
      .add("28/07/2006", "31/07/2006", TransactionType.CREDIT_CARD, "ST MARCEL DIST", "", -32.22)
      .add("29/07/2006", "31/07/2006", TransactionType.CREDIT_CARD, "CENTRE E LECLE", "", -61.85)
      .check();
  }

  public void test2() throws Exception {
    operations.importOfxFile(getFile("laposte2.ofx"));
    timeline.selectMonths("2008/09", "2008/10", "2008/11", "2008/12");
    views.selectData();
    transactions.getTable().getHeader().click(1);
    transactions
      .initContent()
      .add("23/12/2008", "24/12/2008", TransactionType.CREDIT_CARD, "MAG SUPER U", "", -64.87)
      .add("23/12/2008", TransactionType.CHECK, "CHEQUE N°8582011", "", -15.00)
      .add("23/12/2008", TransactionType.VIREMENT, "RGF MFP", "", 7.50)
      .add("23/12/2008", TransactionType.DEPOSIT, "REMISE CHEQUES", "", 12.50)
      .add("22/12/2008", TransactionType.VIREMENT, "XXX .", "", 1688.86)
      .add("15/12/2008", "16/12/2008", TransactionType.CREDIT_CARD, "FREE", "", -36.16)
      .add("15/12/2008", TransactionType.PRELEVEMENT, "PRELEVEMENT SFR", "", -49.39)
      .add("12/12/2008", "15/12/2008", TransactionType.WITHDRAWAL, "RETRAIT VISA 14H11", "", -80.00)
      .add("12/12/2008", TransactionType.PRELEVEMENT, "COFINOGA", "", -13.00)
      .add("10/12/2008", TransactionType.PRELEVEMENT, "FRAIS DE VIREMENT", "", -0.98)
      .add("10/12/2008", TransactionType.PRELEVEMENT, "VIREMENT PERMANENT", "", -820.00)
      .add("24/11/2008", TransactionType.VIREMENT, "VIREMENT", "", 210.00)
      .add("20/11/2008", TransactionType.PRELEVEMENT, "PRELEVEMENT DE ????", "", -10.00)
      .add("10/11/2008", TransactionType.DEPOSIT, "REMISE CHEQUES", "", 69.00)
      .add("21/10/2008", TransactionType.PRELEVEMENT, "FRAIS POUR PRELEVEMENT IMPAYE", "", -10.00)
      .add("03/10/2008", TransactionType.VIREMENT, "REMISE COMMERCIALE D AGIOS", "", 2.21)
      .add("03/10/2008", TransactionType.PRELEVEMENT, "INTERETS DEBITEURS 3ME TRIMESTRE", "", -2.21)
      .add("02/10/2008", TransactionType.PRELEVEMENT, "COTISATION TRIMESTRIELLE", "", -16.15)
      .add("01/10/2008", TransactionType.PRELEVEMENT, "VIREMENT PERMANENT", "", -50.00)
      .add("30/09/2008", "01/10/2008", TransactionType.CREDIT_CARD, "ALINEA 02", "", -9.80)
      .check();
  }

  public void testDeferred() throws Exception {
    setCurrentDate("2011/02/02");
    operations.openPreferences().setFutureMonthsCount(2).validate();
    operations.importFirstQifFileWithDeferred(getFile("laposteCBDiffere.ofx"), "9999999X000");
    timeline.selectAll();

    views.selectData();
    transactions.getTable().getHeader().click(1);
    transactions
      .initContent()
      .add("26/01/2011", TransactionType.CREDIT_CARD, "BIO ANTONY", "", -98.44)
      .add("25/01/2011", TransactionType.CREDIT_CARD, "ESSO RABELAIS", "", -63.01)
      .add("23/01/2011", TransactionType.CREDIT_CARD, "SERRAIT DIDIER", "", -40.93)
      .add("23/01/2011", TransactionType.CREDIT_CARD, "FROMAGERIE ST D", "", -28.53)
      .add("22/01/2011", TransactionType.CREDIT_CARD, "PICARD SA 118", "", -28.85)
      .add("21/01/2011", TransactionType.CREDIT_CARD, "MARCHE ST PIERR", "", -18.60)
      .add("21/01/2011", TransactionType.CREDIT_CARD, "DEPARTEMENT MAR", "", -27.13)
      .add("21/01/2011", TransactionType.CREDIT_CARD, "MERC.ST PIERRE", "", -24.85)
      .add("21/01/2011", TransactionType.CREDIT_CARD, "BIO ANTONY", "", -12.99)
      .add("21/01/2011", TransactionType.CREDIT_CARD, "PHOTOVIT", "", -64.64)
      .add("20/01/2011", TransactionType.CREDIT_CARD, "BIO ANTONY", "", -58.52)
      .add("20/01/2011", TransactionType.CREDIT_CARD, "CORA", "", -76.82)
      .add("19/01/2011", TransactionType.CREDIT_CARD, "PHARMACIE MEDIO", "", -34.50)
      .add("14/01/2011", TransactionType.CREDIT_CARD, "BIO ANTONY", "", -61.46)
      .add("14/01/2011", TransactionType.CREDIT_CARD, "MONCHATRE C", "", -29.41)
      .add("14/01/2011", TransactionType.CREDIT_CARD, "PAYPAL", "", -113.59)
      .add("13/01/2011", TransactionType.CREDIT_CARD, "TAO THIAIS", "", -20.48)
      .add("13/01/2011", TransactionType.CREDIT_CARD, "DECATHLON 468", "", -19.15)
      .add("13/01/2011", TransactionType.CREDIT_CARD, "432-IKEA THIAIS", "", -43.89)
      .add("13/01/2011", TransactionType.CREDIT_CARD, "ALICE DELICE", "", -19.95)
      .add("13/01/2011", TransactionType.CREDIT_CARD, "PARKAGE E-LUDIK", "", -145.11)
      .add("12/01/2011", TransactionType.CREDIT_CARD, "PAYPAL", "", -87.59)
      .add("07/01/2011", TransactionType.CREDIT_CARD, "BIO ANTONY", "", -109.88)
      .add("07/01/2011", TransactionType.CREDIT_CARD, "CARONBIO ANTONY", "", -87.04)
      .add("05/01/2011", TransactionType.CREDIT_CARD, "LE PETRIN RIBEI", "", -25.90)
      .add("05/01/2011", TransactionType.CREDIT_CARD, "KERIA C7", "", -109.00)
      .add("29/12/2010", TransactionType.CREDIT_CARD, "BIO ANTONY", "", -85.76)
      .add("28/12/2010", TransactionType.CREDIT_CARD, "BIO ANTONY", "", -84.01)
      .add("28/12/2010", TransactionType.CREDIT_CARD, "MONCHATRE C", "", -67.29)
      .add("28/12/2010", TransactionType.CREDIT_CARD, "PATHE BELLE EPI", "", -41.60)
      .check();

    operations.importWithNewAccount(getFile("laposteCC.ofx"), "compte courant");

    views.selectData();
    transactions.getTable().getHeader().click(1);
    transactions
      .initContent()
      .add("02/05/2011", TransactionType.VIREMENT, "LA MUTUELLE", "", 10.90)
      .add("02/05/2011", TransactionType.PRELEVEMENT, "CH.CCNELA", "", -100.00)
      .add("03/05/2011", TransactionType.PRELEVEMENT, "ABONNEMENT INTE", "", -72.40)
      .add("03/05/2011", TransactionType.PRELEVEMENT, "CNP ASSURANCES", "", -150.00)
      .add("03/05/2011", TransactionType.PRELEVEMENT, "TELEREGLEMENT DE URSSAF DE", "", -132.00)
      .add("04/05/2011", TransactionType.PRELEVEMENT, "CHEQUE", "", -167.00)
      .add("04/05/2011", TransactionType.PRELEVEMENT, "VE D'ILE DE FRA", "", -86.16)
      .add("05/05/2011", TransactionType.CREDIT_CARD, "DEBIT CARTE BANCAIRE DIFFERE", "", -2629.83)
      .add("05/05/2011", TransactionType.PRELEVEMENT, "MMA IARD SA", "", -41.45)
      .add("05/05/2011", TransactionType.PRELEVEMENT, "ECHEANCE PRET", "", -1361.32)
      .add("06/05/2011", TransactionType.VIREMENT, "C.A.F DU VAL DE", "", 125.78)
      .add("06/05/2011", TransactionType.PRELEVEMENT, "NOVALIS PREVOYA", "", -59.00)
      .add("06/05/2011", TransactionType.PRELEVEMENT, "C R P A", "", -479.00)
      .add("09/05/2011", TransactionType.VIREMENT, "FRANCE TELECOM", "", 118.30)
      .add("10/05/2011", TransactionType.PRELEVEMENT, "CHEQUE", "", -475.00)
      .add("13/05/2011", TransactionType.PRELEVEMENT, "EDF PR SIMM", "", -198.00)
      .check();

    categorization.selectTransaction("DEBIT CARTE BANCAIRE DIFFERE")
      .selectOther().selectDeferred().selectSeries("9999999X000");
  }
}
