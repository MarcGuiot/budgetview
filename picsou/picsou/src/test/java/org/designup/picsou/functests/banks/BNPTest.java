package org.designup.picsou.functests.banks;

import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

import java.io.IOException;

public class BNPTest extends SpecificBankTestCase {

  public void test1() throws Exception {
    checkFile("bnp_Money2000_new.qif");
  }

  public void test2() throws Exception {
    checkFile("bnp_PM5_MMJJAA_new.qif");
  }

  public void test3() throws Exception {
    checkFile("bnp_Quicken2000_new.qif");
  }

  public void test4() throws Exception {
    operations.importQifFile(100.0, getFile("bnp_Herve.qif"), "BNP");
    periods.selectAll();
    transactions
      .initContent()
      .add("06/05/2008", TransactionType.PRELEVEMENT, "EDF PR QE CLIO BNPP NUM 001007 ECH 06.05", "", -24.00)
      .add("06/05/2008", TransactionType.WITHDRAWAL, "RETRAIT 13H46 142170 BNP GERLAND DEBOUR", "", -40.00)
      .add("02/05/2008", TransactionType.PRELEVEMENT, "GAZ DE FRANCE DIRCO NUM 002381 ECH 02.05", "", -43.34)
      .add("30/04/2008", TransactionType.VIREMENT, "REMBOURST CB DU 070408 FUNG WAH BUS CART", "", 36.26)
      .add("30/04/2008", TransactionType.CHECK, "CHEQUE N. 3586770", "", -514.02)
      .add("30/04/2008", TransactionType.CREDIT_CARD, "DU 200408 SNCF INTERNET 77 ISLES LES ME", "", -182.00)
      .add("30/04/2008", TransactionType.CREDIT_CARD, "DU 290308 MARCHE PLUS 69 LYON 8 CARTE 49", "", -14.35)
      .add("30/04/2008", TransactionType.WITHDRAWAL, "RETRAIT 09H07 009062 SAVOIE TECHNOL 000", "", -50.00)
      .add("30/04/2008", TransactionType.INTERNAL_TRANSFER, "VIR. SUR LE P.E.L 123123123123123123123", "", -45.00)
      .add("25/04/2008", TransactionType.PRELEVEMENT, "CGE 31489 NUM 437614 ECH 25.04.08 ARCH03", "", -99.71)
      .check();
  }

  private void checkFile(String path) throws IOException {
    operations.importQifFile(100.0, getFile(path), "BNP");
    periods.selectAll();
    transactions
      .initContent()
      .add("05/06/2008", TransactionType.CHECK, "CHEQUE N. 6872464", "", -20.50, "To categorize")
      .add("03/06/2008", TransactionType.INTERNAL_TRANSFER, "VIR. VTL 03/06 12H13 V06031213 75315 4698764 3", "", 45.00, "To categorize")
      .add("02/06/2008", TransactionType.CREDIT, "PRET 30004024542454643486431", "", -20.50, "To categorize")
      .add("30/05/2008", TransactionType.BANK_FEES, "COMMISSIONS PERCUES POUR TRAITEMENT PARTICULIER D OPERATIONS", "", -20.50, MasterCategory.BANK)
      .add("20/05/2008", TransactionType.VIREMENT, "SECU 05/2008", "", 45.00, "To categorize")
      .add("16/05/2008", TransactionType.PRELEVEMENT, "TRESOR PUBLIC 75 IMPOT NUM 005002 ECH 16", "", -20.50, "To categorize")
      .add("15/05/2008", TransactionType.INTERNAL_TRANSFER, "VIR. SUR LE CODEVI 300044687354357421340", "", -20.50, "To categorize")
      .add("15/05/2008", TransactionType.INTERNAL_TRANSFER, "VIR. SUR LE P.E.L 300040123454643353", "", -20.50, "To categorize")
      .add("12/05/2008", TransactionType.CREDIT, "PRET 3000454337538164843", "", -20.50, "To categorize")
      .add("09/05/2008", TransactionType.PRELEVEMENT, "A G F VIE EN UC NUM 100315 ECH 09.05.08", "", -20.50, "To categorize")
      .add("07/05/2008", TransactionType.CHECK, "CHEQUE N. 6872463", "", -20.50, "To categorize")
      .add("06/05/2008", TransactionType.CREDIT, "PRET 3000442473573763748546", "", -20.50, "To categorize")
      .check();
  }
}
