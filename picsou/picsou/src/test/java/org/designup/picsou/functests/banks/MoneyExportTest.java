package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class MoneyExportTest extends SpecificBankTestCase {
  
  public void testDefaultQifFile() throws Exception {
    operations.openImportDialog()
      .setFilePath(getFile("money_export_standard.qif"))
      .acceptFile()
      .createNewAccount("CIC", "Main account", "", 0.)
      .setMainAccount()
      .importSeries()
      .checkContains("Alimentation", "Loisirs-culture-sport", "Auto-moto")
      .setRecurring("Alimentation")
      .setVariable("Loisirs-culture-sport", "Auto-moto")
      .validate();

    transactions.initAmountContent()
      .add("20/08/2011", "SPLIT COURSES QUELCONQUES", 0.00, "Alimentation", 0.00, 0.00, "Main account")
      .add("20/08/2011", "SPLIT COURSES QUELCONQUES", -20.00, "Loisirs-culture-sport", 0.00, 0.00, "Main account")
      .add("20/08/2011", "SPLIT COURSES QUELCONQUES", -30.00, "Alimentation", 20.00, 20.00, "Main account")
      .add("20/08/2011", "CREDIPLUS CREDIT PORSCHE", -100.00, "Auto-moto", 50.00, 50.00, "Main account")
      .add("04/08/2011", "FNAC VELIZY CARTE 34609231 PAIEM UN LOGICIEL QUELCONQUE", -14.17, "Loisirs-culture-sport", 150.00, 150.00, "Main account")
      .add("04/08/2011", "SOLDE INITIAL", 1000.00, "To categorize", 164.17, 164.17, "Main account")
      .check();

    operations.openImportDialog()
      .setFilePath(getFile("money_export_standard.qif"))
      .acceptFile()
      .importSeries()
      .checkContains("[Test]")
      .setRecurring("[Test]")
      .checkNotContain("Alimentation")
      .validate();

    categorization.selectTransaction("SPLIT COURSES QUELCONQUES");
     transactionDetails.openSplitDialog()
       .checkTable(new Object[][]{
         {"Alimentation / Épicerie", "SPLIT COURSES QUELCONQUES", 0.0, ""},
         {"Alimentation / Épicerie", "SPLIT COURSES QUELCONQUES", -30.00, "Courses quelconques"},
         {"Loisirs-culture-sport / Journaux", "SPLIT COURSES QUELCONQUES", -20.00, "Motomag"},
       });
  }

  public void testStrictQifFile() throws Exception {
    operations.openImportDialog()
    .setFilePath(getFile("money_export_strict.qif"))
    .acceptFile()
    .createNewAccount("CIC", "Main account", "", 0.)
    .setMainAccount()
    .importSeries()
    .cancelImportSeries();

    transactions.initContent()
      .add("20/08/2011", TransactionType.VIREMENT, "SPLIT COURSES QUELCONQUES", "", 0.00)
      .add("20/08/2011", TransactionType.PRELEVEMENT, "SPLIT COURSES QUELCONQUES", "Motomag", -20.00)
      .add("20/08/2011", TransactionType.PRELEVEMENT, "SPLIT COURSES QUELCONQUES", "Courses quelconques", -30.00)
      .add("20/08/2011", TransactionType.PRELEVEMENT, "CREDIPLUS CREDIT PORSCHE", "", -100.00)
      .add("04/08/2011", TransactionType.PRELEVEMENT, "FNAC VELIZY CARTE 34609231 PAIEM UN LOGICIEL QUELCONQUE", "", -14.17)
      .add("04/08/2011", TransactionType.VIREMENT, "SOLDE INITIAL", "", 1000.00)
      .check();

  }
}
