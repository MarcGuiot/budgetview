package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class SGTest extends SpecificBankTestCase {

  public void test1() throws Exception {
    operations.importQifFile(getFile("sg1.qif"), SOCIETE_GENERALE);
    timeline.selectAll();
    transactions.getTable().getHeader().click(1);
    transactions
      .initContent()
      .add("22/04/2006", TransactionType.CREDIT_CARD, "SACLAY", "", -55.49)
      .add("20/04/2006", TransactionType.CREDIT_CARD, "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS", "", -17.65)
      .add("20/04/2006", TransactionType.CREDIT_CARD, "BISTROT ANDRE CARTE 06348905 PAIEMENT CB 1904 015 PARIS", "", -49.00)
      .add("19/04/2006", TransactionType.CREDIT_CARD, "SARL KALISTEA CARTE 06348905 PAIEMENT CB 1404 PARIS", "", -14.50)
      .add("13/04/2006", TransactionType.CREDIT_CARD, "STATION BP MAIL CARTE 06348905 PAIEMENT CB 1104 PARIS", "", -18.70)
      .check();
  }

  public void test2() throws Exception {
    operations.importQifFile(getFile("sg2.qif"), SOCIETE_GENERALE);
    timeline.selectAll();
    transactions.getTable().getHeader().click(1);
    transactions
      .initContent()
      .add("22/07/2006", "24/07/2006", TransactionType.CREDIT_CARD, "ANTONYCARBURANT", "", -45.83)
      .add("21/07/2006", "24/07/2006", TransactionType.WITHDRAWAL, "RETRAIT 12H37 PARIS LAFFITTE 000089", "", -60.00)
      .add("21/07/2006", "21/07/2006", TransactionType.VIREMENT, "21.07 SG 04042 CPT 00050741769", "", -1000.00)
      .add("21/07/2006", "21/07/2006", TransactionType.VIREMENT, "VIREMENT LOGITEL", "", 1000.0)
      .add("28/06/2006", "28/06/2006", TransactionType.CHECK, "CHEQUE N°186", "", -206.04)
      .add("21/06/2006", "21/06/2006", TransactionType.VIREMENT, "AGF ASSET MANAGEME 2006/05", "", 10333.44)
      .add("18/06/2006", "19/06/2006", TransactionType.WITHDRAWAL, "RETRAIT 20H15 ANTONY A PAJEAUD 000009", "", -60.00)
      .add("05/06/2006", "05/06/2006", TransactionType.PRELEVEMENT, "TPS FRA01107365A040606/T.P.S. 000103017914", "", -34.50)
      .add("27/05/2006", "29/05/2006", TransactionType.WITHDRAWAL, "RETRAIT 15H49 ETRETAT 35179000", "", -70.00)
      .add("03/05/2006", "04/05/2006", TransactionType.WITHDRAWAL, "RETRAIT 21H02 MONTLHERY 00904963", "", -50.00)
      .add("24/04/2006", "24/04/2006", TransactionType.CREDIT_CARD, "SARL KALISTEA", "", -100.00)
      .check();
  }

  public void testNewFormat() throws Exception {
    operations.importQifFile(getFile("sg2008.qif"), SOCIETE_GENERALE);
    timeline.selectAll();
    transactions.getTable().getHeader().click(1);
    transactions
      .initContent()
      .add("27/08/2008", "28/08/2008", TransactionType.CREDIT_CARD, "MONOPRIX 098", "", -25.72)
      .add("28/08/2008", "28/08/2008", TransactionType.VIREMENT, "CPAM VAL D'OISE MOTIF: 082400010802 082400010802", "", 36.40)
      .add("26/08/2008", "27/08/2008", TransactionType.CREDIT_CARD, "ED HERBLAY 2", "", -153.35)
      .add("26/08/2008", "27/08/2008", TransactionType.CREDIT_CARD, "LEROY MERLIN", "", -28.25)
      .add("27/08/2008", "27/08/2008", TransactionType.CREDIT_CARD, "LEROY MERLIN", "", -28.25)
      .add("26/08/2008", "26/08/2008", TransactionType.DEPOSIT, "REMISE DE 1 CHEQUE 03281", "", 31.72)
      .add("22/08/2008", "26/08/2008", TransactionType.CREDIT_CARD, "APRR AUTOROUTE", "", -26.20)
      .add("25/08/2008", "25/08/2008", TransactionType.CHECK, "CHEQUE N°628", "", -730.53)
      .add("25/08/2008", "25/08/2008", TransactionType.PRELEVEMENT, "EDF PR QE CLIO BRE *123631470383 21420*728 EDF PR", "", -72.00)
      .add("25/08/2008", "25/08/2008", TransactionType.CHECK, "CHEQUE N°626", "", -110.00)
      .add("25/08/2008", "25/08/2008", TransactionType.WITHDRAWAL, "RETRAIT 08H50 PARIS CHARLES MICHELS 00904015", "", -40.00)
      .add("22/08/2008", "25/08/2008", TransactionType.WITHDRAWAL, "RETRAIT 08H50 LONS LE SAUNIER 01257261", "", -50.00)
      .add("24/08/2008", "25/08/2008", TransactionType.CREDIT_CARD, "MONOPRIX 1254", "", -13.59)
      .add("22/08/2008", "25/08/2008", TransactionType.CREDIT_CARD, "GEANT CG807", "", -73.52)
      .add("22/08/2008", "25/08/2008", TransactionType.CREDIT_CARD, "DAC SUPER U", "", -69.52)
      .add("21/08/2008", "25/08/2008", TransactionType.CREDIT_CARD, "DAC SUPER U", "", -49.52)
      .add("22/08/2008", "22/08/2008", TransactionType.CHECK, "CHEQUE N°627", "", -65.00)
      .add("21/08/2008", "22/08/2008", TransactionType.CREDIT_CARD, "CYBERCENTRALE COMMERCE ELECTRONIQUE", "", -29.99)
      .add("21/08/2008", "22/08/2008", TransactionType.CREDIT_CARD, "LA FOIR'FOUILLE", "", -14.45)
      .add("22/08/2008", "22/08/2008", TransactionType.PRELEVEMENT, "COTISATION MENSUELLE JAZZ DONT CARTE MME GUIOT DU DOIGNON", "", -3.80)
      .add("22/08/2008", "22/08/2008", TransactionType.PRELEVEMENT, "COTISATION MENSUELLE JAZZ DONT CARTE M. GUIOT DU DOIGNON", "", -7.60)
      .add("22/08/2008", "22/08/2008", TransactionType.VIREMENT, "F. M. P. MOTIF: MUTUELLE MNPAF", "", 43.02)
      .add("20/08/2008", "21/08/2008", TransactionType.WITHDRAWAL, "RETRAIT 16H24 LONS LE SAUNIER 00902406", "", -30.00)
      .add("20/08/2008", "21/08/2008", TransactionType.CREDIT_CARD, "MAG SUPER U", "", -36.87)
      .add("19/08/2008", "19/08/2008", TransactionType.PRELEVEMENT, "GAZ DE FRANCE DIRC 5000043251270004028*15780", "", -96.46)
      .add("19/08/2008", "19/08/2008", TransactionType.PRELEVEMENT, "GRAS SAVOYE ASS. CLUBS BOUYGUESDN0000429742 *000957 DN0000429742", "", -4.50)
      .add("18/08/2008", "19/08/2008", TransactionType.CREDIT_CARD, "BAR LE TONNEAU", "", -23.80)
      .add("18/08/2008", "19/08/2008", TransactionType.CREDIT_CARD, "MAG SUPER U", "", -18.28)
      .add("18/08/2008", "18/08/2008", TransactionType.CHECK, "CHEQUE N°624", "", -100.00)
      .add("16/08/2008", "18/08/2008", TransactionType.CREDIT_CARD, "INTERMARCHE", "", -66.00)
      .add("14/08/2008", "18/08/2008", TransactionType.CREDIT_CARD, "LIB DES ARCADES", "", -56.60)
      .add("14/08/2008", "18/08/2008", TransactionType.CREDIT_CARD, "MAG SUPER U", "", -38.81)
      .add("14/08/2008", "18/08/2008", TransactionType.CREDIT_CARD, "SNCF INTERNET COMMERCE ELECTRONIQUE", "", -21.60)
      .add("14/08/2008", "18/08/2008", TransactionType.CREDIT_CARD, "MAG SUPER U", "", -11.00)
      .check();
  }
}
