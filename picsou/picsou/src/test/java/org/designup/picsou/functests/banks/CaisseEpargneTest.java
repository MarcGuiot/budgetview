package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class CaisseEpargneTest extends SpecificBankTestCase {
  public void test() throws Exception {
    operations.importQifFile(10., getFile("caisse_epargne.qif"), "Caisse d'épargne");
    timeline.selectMonths("2008/07", "2008/08", "2008/09");
    transactions
      .initContent()
      .add("05/09/2008", TransactionType.PRELEVEMENT, "*HABITATION 001919425", "", -33.00)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "*PJ 001919414", "", -4.00)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "12743YO43PH93175", "", -23.18)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "A40 001 5 35 5 239300 00", "", -49.00)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "ECH PRET 0160301 DATE 20080905", "", -1125.99)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "ECH PRET 0160302 DATE 20080905", "", -665.73)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "ECH PRET 0187126 DATE 20080905", "", -173.02)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "ECH PRET 0189650 DATE 20080905", "", -184.36)
      .add("05/09/2008", TransactionType.VIREMENT, "P 7246098DWEULERSSE   082008ME", "", 1118.80)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "RETRAIT DAB CHABRIERES GIF080904", "", -20.00)
      .add("04/09/2008", TransactionType.PRELEVEMENT, "CARTE LIDL     1493       020908", "", -56.00)
      .add("03/09/2008", TransactionType.PRELEVEMENT, "CARTE CHAMPION 7926MG     010908", "", -84.93)
      .add("03/09/2008", TransactionType.PRELEVEMENT, "CARTE COFIROUTE           300808", "", -41.50)
      .add("03/09/2008", TransactionType.PRELEVEMENT, "CARTE COOP 031 AVAILL     300808", "", -19.23)
      .add("03/09/2008", TransactionType.PRELEVEMENT, "CARTE EDNOR BRICOMARCH    300808", "", -43.90)
      .add("03/09/2008", TransactionType.PRELEVEMENT, "CARTE GARAGE YVES MORE    300808", "", -30.00)
      .add("03/09/2008", TransactionType.PRELEVEMENT, "CARTE MONDIS VAL DE LO    300808", "", -61.00)
      .add("03/09/2008", TransactionType.PRELEVEMENT, "CARTE PRESSING CHEVRY     010908", "", -69.00)
      .add("03/09/2008", TransactionType.PRELEVEMENT, "CHEQUE N?5364043", "", -24.00)
      .add("02/09/2008", TransactionType.PRELEVEMENT, "RETRAIT DAB CHABRIERES GIF080830", "", -30.00)
      .add("30/08/2008", TransactionType.PRELEVEMENT, "CARTE EBAY EUROPESARL     220808", "", -3.90)
      .add("29/08/2008", TransactionType.VIREMENT, "314 694 187181 010908681 681", "", 4359.61)
      .add("28/08/2008", TransactionType.PRELEVEMENT, "CARTE 1001 LISTES         260808", "", -40.00)
      .add("28/08/2008", TransactionType.PRELEVEMENT, "CARTE GA.LAFAYETTE VAD    260808", "", -41.00)
      .add("28/08/2008", TransactionType.PRELEVEMENT, "CARTE SAPN 2108           210808", "", -10.60)
      .add("26/08/2008", TransactionType.PRELEVEMENT, "CARTE CARREFOUR ULIS      220808", "", -32.59)
      .add("26/08/2008", TransactionType.PRELEVEMENT, "CARTE SNCF                240808", "", -16.80)
      .add("26/08/2008", TransactionType.PRELEVEMENT, "CARTE SNCF INTERNET       220808", "", -61.20)
      .add("26/08/2008", TransactionType.PRELEVEMENT, "CARTE SNCF INTERNET       240808", "", -36.20)
      .add("23/08/2008", TransactionType.PRELEVEMENT, "CARTE EDNOR BRICOMARCH    210808", "", -13.35)
      .add("23/08/2008", TransactionType.PRELEVEMENT, "CARTE HALLE CHAUSSURES    200808", "", -20.93)
      .add("23/08/2008", TransactionType.PRELEVEMENT, "CARTE INTERMARCHE         210808", "", -35.60)
      .add("23/08/2008", TransactionType.PRELEVEMENT, "CARTE JARDIN GALLY        200808", "", -33.00)
      .add("23/08/2008", TransactionType.PRELEVEMENT, "CARTE JARDINERIE CHEV     210808", "", -108.00)
      .add("22/08/2008", TransactionType.PRELEVEMENT, "CARTE EDNOR BRICOMARCH    200808", "", -30.80)
      .add("22/08/2008", TransactionType.PRELEVEMENT, "CARTE INTERMARCHE         200808", "", -122.60)
      .add("22/08/2008", TransactionType.PRELEVEMENT, "CARTE SODEXO FR000427     200808", "", -50.00)
      .add("21/08/2008", TransactionType.PRELEVEMENT, "RETRAIT DAB CHABRIERES GIF080820", "", -20.00)
      .add("20/08/2008", TransactionType.PRELEVEMENT, "CARTE CHAMPION 7926MG     180808", "", -138.77)
      .add("20/08/2008", TransactionType.PRELEVEMENT, "CARTE INTERMARCHE S/S     170808", "", -45.16)
      .add("20/08/2008", TransactionType.VIREMENT, "REMISE N?:1037870-000003 CHEQUE", "", 481.93)
      .check();
  }
}
