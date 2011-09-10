package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class MoneyExportTest extends SpecificBankTestCase {
  public void testDefaultQifFile() throws Exception {
    operations.importQifFile(getFile("money_export_standard.qif"), "CIC", 0.);
    transactions.initContent()
      .add("20/08/2011", TransactionType.PRELEVEMENT, "SPLIT COURSES QUELCONQUES", "", -50.00)
      .add("20/08/2011", TransactionType.PRELEVEMENT, "CREDIPLUS CREDIT PORSCHE", "", -100.00)
      .add("04/08/2011", TransactionType.PRELEVEMENT, "FNAC VELIZY CARTE 34609231 PAIEM UN LOGICIEL QUELCONQUE", "", -14.17)
      .add("04/08/2011", TransactionType.VIREMENT, "SOLDE INITIAL", "", 1000.00)
      .check();
  }

  public void testStrictQifFile() throws Exception {
    operations.importQifFile(getFile("money_export_strict.qif"), "CIC", 0.);
    transactions.initContent()
      .add("20/08/2011", TransactionType.PRELEVEMENT, "SPLIT COURSES QUELCONQUES", "", -50.00)
      .add("20/08/2011", TransactionType.PRELEVEMENT, "CREDIPLUS CREDIT PORSCHE", "", -100.00)
      .add("04/08/2011", TransactionType.PRELEVEMENT, "FNAC VELIZY CARTE 34609231 PAIEM UN LOGICIEL QUELCONQUE", "", -14.17)
      .add("04/08/2011", TransactionType.VIREMENT, "SOLDE INITIAL", "", 1000.00)
      .check();

  }
}
