package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class BanquePopulaireTest extends SpecificBankTestCase {

  public void test() throws Exception {
    operations.importOfxFile(getFile("banque_pop.ofx"));
    timeline.selectAll();
    transactions.getTable().getHeader().click(1);

    transactions.initContent()
      .add("13/02/2009", TransactionType.PRELEVEMENT, "PRELEVEMENT DE MR OU MME XYZ", "", -100.00)
      .add("13/02/2009", TransactionType.PRELEVEMENT, "FT INTERNET ORANGE 432123 123456790WFE N.EMETTEUR: 422262", "", -46.33)
      .add("11/02/2009", TransactionType.CHECK, "CHEQUE N°0004228", "", -25.40)
      .add("10/02/2009", TransactionType.DEPOSIT, "REMISE DE 1 CHEQUE", "", 60.00)
      .add("10/02/2009", TransactionType.VIREMENT, "VIREMENT DE MME AZE AZE", "", 1000.00)
      .add("09/02/2009", "10/02/2009", TransactionType.WITHDRAWAL, "RETRAIT GAB LONS SULLY 2 *123123123 A 10:24", "", -30.00)
      .add("10/02/2009", TransactionType.PRELEVEMENT, "SOCRAM BANQUE *PRLV 37589392 N.EMETTEUR: 003476", "", -301.10)
      .add("09/02/2009", TransactionType.VIREMENT, "VIREMENT DE RSI FRANCHE COMTE", "", 18.09)
      .add("09/02/2009", TransactionType.VIREMENT, "VIREMENT DE XXXX XXXX XXXX", "", 507.17)
      .add("05/02/2009", TransactionType.PRELEVEMENT, "COTIS EQUIPAGE HT: 5,69/TVA19,60: 0,13 EQUIPAGE BPBFC N 123123", "", -5.82)
      .add("05/02/2009", TransactionType.PRELEVEMENT, "CREDIPAR CITROEN *100G0473733 32 050209 N.EMETTEUR: 126896", "", -56.34)
      .add("30/01/2009", TransactionType.PRELEVEMENT, "CARTE FACTURETTES CB VOTRE RELEVE ARRETE AU 30/01/09", "", -200.10)
      .add("29/01/2009", TransactionType.PRELEVEMENT, "DROITS DE GARDE 2009 DU COMPTE 123123123123", "", -35.50)
      .add("26/01/2009", TransactionType.PRELEVEMENT, "ECHEANCE PRET DONT CAP 67,52 ASS. 0,00E INT. 14,36 COM. 0,00E", "", -81.88)
      .add("26/01/2009", TransactionType.VIREMENT, "VIREMENT DE C.P.A.M LONS", "", 12.90)
      .check();
  }

  public void testWithDifferedAccount() throws Exception {
    operations.importOfxFile(getFile("banque_pop.ofx"));
    operations.importOfxFile(getFile("banque_pop_facturette.ofx"));
    operations.importOfxFile(getFile("banque_pop_en_cours.ofx"));
    views.selectCategorization();
    timeline.selectAll();
    categorization.selectTransaction("CARTE FACTURETTES CB VOTRE RELEVE ARRETE AU 30/01/09")
      .selectOther()
      .selectDeferred()
      .checkActiveSeries("Card n. 123123123")
      .selectSeries("Card n. 123123123");

    views.selectData();
    transactions.initAmountContent()
      .add("18/02/2009", "MAG SUPER U 39MONTMOROT", -30.82, "To categorize", -554.53, 1976.64, "Card n. 123123123")
      .add("17/02/2009", "SCP DE MEDECINS 39LONS LE SAUNI", -47.88, "To categorize", -523.71, 2007.46, "Card n. 123123123")
      .add("17/02/2009", "MILLE ET UNE CO 39MESSIA SUR S", -19.00, "To categorize", -475.83, 2055.34, "Card n. 123123123")
      .add("17/02/2009", "MAG SUPER U 39MONTMOROT", -11.54, "To categorize", -456.83, 2074.34, "Card n. 123123123")
      .add("16/02/2009", "MAG SUPER U 39MONTMOROT", -19.63, "To categorize", -445.29, 2085.88, "Card n. 123123123")
      .add("15/02/2009", "SPAR SUPERMARCH 39LONS LE SR", -16.20, "To categorize", -425.66, 2105.51, "Card n. 123123123")
      .add("13/02/2009", "PRELEVEMENT DE MR OU MME XYZ", -100.00, "To categorize", 2705.72, 2531.17, "Account n. 123123123")
      .add("13/02/2009", "FT INTERNET ORANGE 432123 123456790WFE N.EMETTEUR: 422262", -46.33, "To categorize", 2805.72, 2631.17, "Account n. 123123123")
      .add("12/02/2009", "MAG SUPER U 39MONTMOROT", -104.66, "To categorize", -409.46, 2121.71, "Card n. 123123123")
      .add("12/02/2009", "MAG SUPER U 39MONTMOROT", -18.59, "To categorize", -304.80, 2226.37, "Card n. 123123123")
      .add("11/02/2009", "CHEQUE N°0004228", -25.40, "To categorize", 2852.05, 2677.50, "Account n. 123123123")
      .add("10/02/2009", "REMISE DE 1 CHEQUE", 60.00, "To categorize", 2877.45, 2702.90, "Account n. 123123123")
      .add("10/02/2009", "VIREMENT DE MME AZE AZE", 1000.00, "To categorize", 2817.45, 2642.90, "Account n. 123123123")
      .add("10/02/2009", "SOCRAM BANQUE *PRLV 37589392 N.EMETTEUR: 003476", -301.10, "To categorize", 1847.45, 1672.90, "Account n. 123123123")
      .add("09/02/2009", "INTERMARCHE 39LONS LE SAUNI", -99.21, "To categorize", -286.21, 2244.96, "Card n. 123123123")
      .add("09/02/2009", "RETRAIT GAB LONS SULLY 2 *123123123 A 10:24", -30.00, "To categorize", 1817.45, 1642.90, "Account n. 123123123")
      .add("09/02/2009", "VIREMENT DE RSI FRANCHE COMTE", 18.09, "To categorize", 2148.55, 1974.00, "Account n. 123123123")
      .add("09/02/2009", "VIREMENT DE XXXX XXXX XXXX", 507.17, "To categorize", 2130.46, 1955.91, "Account n. 123123123")
      .add("07/02/2009", "BURTON 104 39LONS LE SAUNI", -44.90, "To categorize", -187.00, 2344.17, "Card n. 123123123")
      .add("07/02/2009", "MAG SUPER U 39MONTMOROT", -32.50, "To categorize", -142.10, 2389.07, "Card n. 123123123")
      .add("05/02/2009", "COTIS EQUIPAGE HT: 5,69/TVA19,60: 0,13 EQUIPAGE BPBFC N 123123", -5.82, "To categorize", 1623.29, 1448.74, "Account n. 123123123")
      .add("05/02/2009", "CREDIPAR CITROEN *100G0473733 32 050209 N.EMETTEUR: 126896", -56.34, "To categorize", 1629.11, 1454.56, "Account n. 123123123")
      .add("03/02/2009", "PHARMA SALINES 39MONTMOROT", -34.05, "To categorize", -109.60, 2421.57, "Card n. 123123123")
      .add("03/02/2009", "SCP DE MEDECINS 39LONS LE SAUNI", -47.88, "To categorize", -75.55, 2455.62, "Card n. 123123123")
      .add("03/02/2009", "MAG SUPER U 39MONTMOROT", -27.67, "To categorize", -27.67, 2503.50, "Card n. 123123123")
      .add("30/01/2009", "CARTE FACTURETTES CB VOTRE RELEVE ARRETE AU 30/01/09", -200.10, "Card n. 123123123", 1685.45, 1570.15, "Account n. 123123123")
      .add("29/01/2009", "DROITS DE GARDE 2009 DU COMPTE 123123123123", -35.50, "To categorize", 1885.55, 1770.25, "Account n. 123123123")
      .add("26/01/2009", "ECHEANCE PRET DONT CAP 67,52 ASS. 0,00E INT. 14,36 COM. 0,00E", -81.88, "To categorize", 1921.05, 1805.75, "Account n. 123123123")
      .add("26/01/2009", "VIREMENT DE C.P.A.M LONS", 12.90, "To categorize", 2002.93, 1887.63, "Account n. 123123123")
      .add("24/01/2009", "INSTITUT BEAUTE 39LONS LE SAUNI", -30.00, "To categorize", -200.10, 1510.90, "Card n. 123123123")
      .add("06/01/2009", "MAG SUPER U 39MONTMOROT", -29.25, "To categorize", -170.10, 1540.90, "Card n. 123123123")
      .add("31/12/2008", "MAG SUPER U 39MONTMOROT", -39.50, "To categorize", -115.30, 1874.73, "Card n. 123123123")
      .add("31/12/2008", "SA CAFREDAU 39MONTMOROT", -50.50, "To categorize", -75.80, 1914.23, "Card n. 123123123")
      .add("31/12/2008", "MAILLARD D 39LONS/SAUNIER", -25.30, "To categorize", -25.30, 1964.73, "Card n. 123123123")
      .check();
  }
}