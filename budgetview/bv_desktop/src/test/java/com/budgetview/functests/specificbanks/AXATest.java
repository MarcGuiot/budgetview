package com.budgetview.functests.specificbanks;

import com.budgetview.model.TransactionType;
import org.junit.Test;

public class AXATest extends SpecificBankTestCase {

  @Test
  public void testQif() throws Exception {
    operations.importQifFile(getFile("axa.qif"), "AXA Banque", 0.);
    timeline.selectAll();

    views.selectData();
    transactions.getTable().getHeader().click(1);
    transactions
      .initContent()
      .add("30/09/2008", TransactionType.PRELEVEMENT, "ACHATS CARTE", "", -1298.51)
      .add("27/09/2008", "29/09/2008", TransactionType.CREDIT_CARD, "MR JEAN CLAUDE", "", -35.00)
      .add("04/09/2008", "26/09/2008", TransactionType.CREDIT_CARD, "SWEETMOMMY ETRANG", "", -255.76)
      .add("25/09/2008", TransactionType.VIREMENT, "AGF ASSET MANAGEMENT", "", 9990.00)
      .add("20/09/2008", "22/09/2008", TransactionType.CREDIT_CARD, "PRINTEMPS HAUSS", "", -73.50)
      .add("22/09/2008", TransactionType.PRELEVEMENT, "F-COTISATION CARTE", "", -1.35)
      .add("10/09/2008", "18/09/2008", TransactionType.CREDIT_CARD, "YA-MAN ETRANG", "", -103.18)
      .add("18/09/2008", TransactionType.VIREMENT, "AON FRANCE", "", 53.90)
      .add("18/09/2008", TransactionType.PRELEVEMENT, "PRLV ELECTRICITE DE FRANCE", "", -48.91)
      .add("19/08/2008", "17/09/2008", TransactionType.CREDIT_CARD, "PRIME SHOPPI ETRANG", "", -613.26)
      .add("16/09/2008", TransactionType.VIREMENT, "CPAM DE PARIS", "", 16.10)
      .add("16/09/2008", TransactionType.PRELEVEMENT, "PRLV TRESOR PUBLIC 75 IMPOT", "", -200.00)
      .add("12/09/2008", "15/09/2008", TransactionType.CREDIT_CARD, "MARCADET EXPL", "", -50.57)
      .add("12/09/2008", TransactionType.WITHDRAWAL, "RETRAIT /09 PARIS DUHESM DAB 10", "", -60.00)
      .add("11/09/2008", TransactionType.PRELEVEMENT, "F-ABON GARANTIES INTERNET", "", -1.50)
      .add("09/09/2008", TransactionType.WITHDRAWAL, "RETRAIT /09 LCL PARIS GD DAB 05", "", -60.00)
      .add("09/09/2008", TransactionType.PRELEVEMENT, "PRLV BOUYGUES TELECOM", "", -19.90)
      .add("04/09/2008", "05/09/2008", TransactionType.CREDIT_CARD, "ISAF", "", -229.00)
      .add("03/09/2008", "05/09/2008", TransactionType.CREDIT_CARD, "MONOPRIX 1162", "", -31.67)
      .add("05/09/2008", TransactionType.VIREMENT, "C.A.F DE PARIS", "", 120.32)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "PRLV NOOS S.A.", "", -31.90)
      .add("04/09/2008", TransactionType.CHECK, "CHEQUE N°5521077", "", -61.60)
      .add("03/09/2008", "04/09/2008", TransactionType.CREDIT_CARD, "CHAUSSURE ROGER", "", -35.00)
      .add("28/08/2008", "03/09/2008", TransactionType.CREDIT_CARD, "WORLD KETAI ETRANG", "", -78.01)
      .add("02/09/2008", TransactionType.WITHDRAWAL, "RETRAIT /08 LCL PARIS GD DAB 29", "", -80.00)
      .add("01/09/2008", "02/09/2008", TransactionType.CREDIT_CARD, "MARCADET EXPL", "", -73.81)
      .add("28/08/2008", "01/09/2008", TransactionType.CREDIT_CARD, "NARITA INT.A ETRANG", "", -18.49)
      .add("28/08/2008", "01/09/2008", TransactionType.CREDIT_CARD, "NARITA AIRPO ETRANG", "", -79.72)
      .check();
  }

  @Test
  public void testOfx() throws Exception {
    operations.importOfxFile(getFile("axa-banque.ofx"));
    timeline.selectAll();

    views.selectData();
    transactions.initContent()
      .add("10/05/2012", TransactionType.PRELEVEMENT, "RETRAIT DAB 09/05 VINCENNES", "", -300.00)
      .add("10/05/2012", TransactionType.PRELEVEMENT, "CHEQUE N°6711256", "", -140.30)
      .add("09/05/2012", TransactionType.VIREMENT, "VIR RECU CNAVTS", "", 1094.71)
      .add("09/05/2012", TransactionType.PRELEVEMENT, "CHEQUE N°6711257", "", -1658.45)
      .add("09/05/2012", TransactionType.PRELEVEMENT, "CHEQUE N°6711258", "", -246.00)
      .add("07/05/2012", TransactionType.PRELEVEMENT, "PRLV UGC ILLIMITE", "", -20.08)
      .add("07/05/2012", TransactionType.PRELEVEMENT, "PRLV NC NUMERICABLE", "", -69.78)
      .add("02/05/2012", TransactionType.PRELEVEMENT, "TIP URSSAF PARIS", "", -374.00)
      .add("02/05/2012", TransactionType.PRELEVEMENT, "TIP IMPOT CEC", "", -1819.00)
      .add("30/04/2012", TransactionType.PRELEVEMENT, "ACHATS CARTE", "", -1196.79)
      .add("27/04/2012", TransactionType.VIREMENT, "VIR RECU BABIN D", "", 8000.00)
      .check();
  }
}