package org.designup.picsou.functests.banks;

import org.designup.picsou.model.TransactionType;

public class MoneyExportTest extends SpecificBankTestCase {

  protected void setUp() throws Exception {
    createDefaultSeries = true;
    resetWindow();
    super.setUp();
  }


  public void __testDefaultQifFile() throws Exception {
    operations.openPreferences().setFutureMonthsCount(3).validate();
    operations.openImportDialog()
      .setFilePath(getFile("money_export_standard.qif"))
      .acceptFile()
      .createNewAccount("CIC", "Main account", "", 0.)
      .setMainAccount()
      .importSeries()
      .checkContains("Loisirs-culture-sport:Journaux",
                     "Auto-moto:Remboursement de pret auto-moto", "Alimentation:Epicerie", "Auto-moto:Essence")
      .setRecurring("Alimentation:Epicerie", "Auto-moto:Remboursement de pret auto-moto")
      .setVariable("Loisirs-culture-sport:Journaux", "Loisirs-culture-sport", "Auto-moto:Essence")
      .validateAndFinishImport();

    transactions.initAmountContent()
      .add("20/08/2008", "SPLIT COURSES QUELCONQUES", 0.00, "Alimentation", 0.00, 0.00, "Main account")
      .add("20/08/2008", "SPLIT COURSES QUELCONQUES", -20.00, "Loisirs-culture-sport", 0.00, 0.00, "Main account")
      .add("20/08/2008", "SPLIT COURSES QUELCONQUES", -30.00, "Alimentation", 20.00, 20.00, "Main account")
      .add("20/08/2008", "ESSO", -100.00, "Auto-moto", 50.00, 50.00, "Main account")
      .add("20/08/2008", "CREDIPLUS CREDIT PORSCHE", -100.00, "Auto-moto", 150.00, 150.00, "Main account")
      .add("04/08/2008", "FNAC VELIZY CARTE 34609231 PAIEM UN LOGICIEL QUELCONQUE", -14.17, "Loisirs-culture-sport", 250.00, 250.00, "Main account")
      .add("04/08/2008", "SOLDE INITIAL", 1000.00, "To categorize", 264.17, 264.17, "Main account")
      .check();

    operations.openImportDialog()
      .setFilePath(getFile("money_export_standard.qif"))
      .acceptFile()
      .importSeries()
      .checkContains("[Test]")
      .setRecurring("[Test]")
      .checkNotContain("Alimentation")
      .validateAndFinishImport();

    categorization.selectTransaction("SPLIT COURSES QUELCONQUES");
    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"Alimentation / Epicerie", "SPLIT COURSES QUELCONQUES", 0.0, ""},
        {"Alimentation / Epicerie", "SPLIT COURSES QUELCONQUES", -30.00, "Courses quelconques"},
        {"Loisirs-culture-sport / Journaux", "SPLIT COURSES QUELCONQUES", -20.00, "Motomag"},
      });

    budgetView.variable
      .checkPlannedUnset("Loisirs-culture-sport");
    budgetView.variable
      .editSeries("Loisirs-culture-sport")
      .checkChart(new Object[][]{
        {"2008", "Aug", 34.17, 0.00, true},
        {"2008", "Sep", 0.00, 0.00, true},
        {"2008", "Oct", 0.00, 0.00, true},
        {"2008", "Nov", 0.00, 0.00, true},
      })
      .cancel();

    budgetView.recurring
      .editSeries("Alimentation")
      .checkChart(new Object[][]{
        {"2008", "Aug", 30.00, 30.00, true},
        {"2008", "Sep", 0.00, 30.00, },
        {"2008", "Oct", 0.00, 30.00, },
        {"2008", "Nov", 0.00, 30.00, },
      })
      .cancel();
  }

  public void __testStrictQifFile() throws Exception {
    operations.openImportDialog()
      .setFilePath(getFile("money_export_strict.qif"))
      .acceptFile()
      .createNewAccount("CIC", "Main account", "", 0.)
      .setMainAccount()
      .importSeries()
      .cancelImportSeries();

    transactions.initContent()
      .add("20/08/2008", TransactionType.VIREMENT, "SPLIT COURSES QUELCONQUES", "", 0.00)
      .add("20/08/2008", TransactionType.PRELEVEMENT, "SPLIT COURSES QUELCONQUES", "Motomag", -20.00)
      .add("20/08/2008", TransactionType.PRELEVEMENT, "SPLIT COURSES QUELCONQUES", "Courses quelconques", -30.00)
      .add("20/08/2008", TransactionType.PRELEVEMENT, "CREDIPLUS CREDIT PORSCHE", "", -100.00)
      .add("04/08/2008", TransactionType.PRELEVEMENT, "FNAC VELIZY CARTE 34609231 PAIEM UN LOGICIEL QUELCONQUE", "", -14.17)
      .add("04/08/2008", TransactionType.VIREMENT, "SOLDE INITIAL", "", 1000.00)
      .check();

  }
}
