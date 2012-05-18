package org.designup.picsou.functests.specificbanks;

import org.designup.picsou.model.TransactionType;

public class LCLTest extends SpecificBankTestCase {
  public void testOFC() throws Exception {
    operations.importOfxFile(getFile("lcl_money.ofc"), 0.);
    checkContent();
  }

  public void testOFX_OFC() throws Exception {
    operations.importOfxFile(getFile("lcl_money_ofx.ofc"));
    checkContent();
  }

  public void testMoneyQifDateFr() throws Exception {
    operations.importQifFile(getFile("lcl_money_date_fr.qif"), "LCL", 0.);
    checkContent();
  }

  public void testMoneyQifDateUs() throws Exception {
    operations.importQifFile(getFile("lcl_money_date_us.qif"), "LCL", 0.);
    checkContent();
  }

  public void testQuickenQifDateFr() throws Exception {
    operations.importQifFile(getFile("lcl_quicken_date_fr.qif"), "LCL", 0.);
    checkContent();
  }

  public void testQuickenQifDateUs() throws Exception {
    operations.importQifFile(getFile("lcl_quicken_date_us.qif"), "LCL", 0.);
    checkContent();
  }

  public void testTousComptesFaitsQifDateFr() throws Exception {
    operations.importQifFile(getFile("lcl_tout_compte_fait_date_fr.qif"), "LCL", 0.);
    checkContent();
  }

  public void testImportAll() throws Exception {
    operations.importQifFile(getFile("lcl_tout_compte_fait_date_fr.qif"), "LCL", 0.);
    operations.importQifFile(getFile("lcl_quicken_date_us.qif"), "LCL");
    operations.importQifFile(getFile("lcl_quicken_date_fr.qif"), "LCL");
    operations.importQifFile(getFile("lcl_money_date_us.qif"), "LCL");
    operations.importQifFile(getFile("lcl_money_date_fr.qif"), "LCL");
    checkContent();
  }

  private void checkContent() throws Exception {
    timeline.selectAll();
    views.selectData();
    transactions.getTable().getHeader().click(1);
    transactions.initContent()
      .add("22/06/2008", "27/06/2008", TransactionType.CREDIT_CARD, "SOFREMI SARL", "", -21.59)
      .add("24/06/2008", "26/06/2008", TransactionType.CREDIT_CARD, "SM CASINO CS494", "", -36.42)
      .add("25/06/2008", TransactionType.VIREMENT, "ACS/FRAIS GENERAUX", "", 2228.79)
      .add("24/06/2008", "25/06/2008", TransactionType.CREDIT_CARD, "LRP DEFE2", "", -30.00)
      .add("24/06/2008", TransactionType.CREDIT_CARD, "RATP", "", -16.00)
      .add("24/06/2008", TransactionType.WITHDRAWAL, "RETRAIT", "", -60.00)
      .add("21/06/2008", "23/06/2008", TransactionType.CREDIT_CARD, "SM CASINO CS494", "", -218.50)
      .add("20/06/2008", TransactionType.VIREMENT, "GESTION BCAC", "", 14.60)
      .add("20/06/2008", TransactionType.CHECK, "CHEQUE N°364966", "", -35.00)
      .add("18/06/2008", "19/06/2008", TransactionType.CREDIT_CARD, "CANTINE LOISIRS", "", -124.80)
      .add("18/06/2008", TransactionType.VIREMENT, "CPAM DES YVELINES", "", 15.40)
      .add("18/06/2008", TransactionType.CHECK, "CHEQUE N°364965", "", -30.00)
      .add("18/06/2008", TransactionType.WITHDRAWAL, "RETRAIT", "", -60.00)
      .add("17/06/2008", TransactionType.CHECK, "CHEQUE N°364964", "", -29.00)
      .add("14/06/2008", "16/06/2008", TransactionType.CREDIT_CARD, "PICARD SA 268", "", -6.10)
      .add("13/06/2008", "16/06/2008", TransactionType.CREDIT_CARD, "SYMPA CLUB II", "", -153.00)
      .add("12/06/2008", "13/06/2008", TransactionType.CREDIT_CARD, "PHARM.DE LA GARE", "", -13.00)
      .add("12/06/2008", "13/06/2008", TransactionType.WITHDRAWAL, "RETRAIT", "", -60.00)
      .add("11/06/2008", "12/06/2008", TransactionType.CREDIT_CARD, "SM CASINO CS494", "", -29.30)
      .add("11/06/2008", "12/06/2008", TransactionType.CREDIT_CARD, "SACCOMANO", "", -65.00)
      .add("11/06/2008", TransactionType.CHECK, "CHEQUE N°364963", "", -40.00)
      .add("08/06/2008", "09/06/2008", TransactionType.CREDIT_CARD, "FRANPRIX", "", -20.31)
      .add("05/06/2008", TransactionType.VIREMENT, "CAF ST QUENTIN EN YVELIN", "", 308.31)
      .add("04/06/2008", "05/06/2008", TransactionType.CREDIT_CARD, "DIONISI FLEURIST", "", -18.00)
      .add("04/06/2008", "05/06/2008", TransactionType.CREDIT_CARD, "SNCF", "", -24.40)
      .add("04/06/2008", "05/06/2008", TransactionType.CREDIT_CARD, "SNCF", "", -28.00)
      .add("03/06/2008", "04/06/2008", TransactionType.CREDIT_CARD, "LRP DEFE2", "", -30.00)
      .add("03/06/2008", "04/06/2008", TransactionType.CREDIT_CARD, "PECCA 206", "", -12.99)
      .add("03/06/2008", "04/06/2008", TransactionType.WITHDRAWAL, "RETRAIT", "", -80.00)
      .add("02/06/2008", "03/06/2008", TransactionType.CREDIT_CARD, "DR GARRIGUES", "", -55.00)
      .add("03/06/2008", TransactionType.CREDIT_CARD, "RATP", "", -16.00)
      .add("01/06/2008", "02/06/2008", TransactionType.CREDIT_CARD, "DURANT ET FILS", "", -22.50)
      .add("28/05/2008", "29/05/2008", TransactionType.CREDIT_CARD, "SM CASINO CS494", "", -11.83)
      .add("28/05/2008", "29/05/2008", TransactionType.WITHDRAWAL, "RETRAIT", "", -60.00)
      .check();
  }
}
