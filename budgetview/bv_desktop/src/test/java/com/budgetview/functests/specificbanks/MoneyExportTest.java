package com.budgetview.functests.specificbanks;

import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.model.TransactionType;
import org.junit.Test;

import java.io.IOException;

public class MoneyExportTest extends SpecificBankTestCase {

  protected void setUp() throws Exception {
    createDefaultSeries = true;
    resetWindow();
    super.setUp();
  }

  @Test
  public void testDefaultQifFile() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/01", 15.00, "internet")
      .load();
    categorization.setRecurring("internet", "Internet");
    operations.openPreferences().setFutureMonthsCount(3).validate();
    importStandard();

    transactions.initAmountContent()
      .add("20/08/2008", "SPLIT COURSES QUELCONQUES", 0.00, "Alimentation / Epicerie", 0.00, 0.00, "Main account")
      .add("20/08/2008", "SPLIT COURSES QUELCONQUES", -20.00, "Loisirs-culture-sport / Journaux", 0.00, 0.00, "Main account")
      .add("20/08/2008", "SPLIT COURSES QUELCONQUES", -30.00, "Alimentation / Epicerie", 20.00, 20.00, "Main account")
      .add("20/08/2008", "ESSO", -100.00, "Auto-moto / Essence", 50.00, 50.00, "Main account")
      .add("20/08/2008", "CREDIPLUS CREDIT PORSCHE", -100.00, "Auto-moto / Remboursement de pret auto-moto", 150.00, 150.00, "Main account")
      .add("04/08/2008", "FNAC VELIZY CARTE 34609231 PAIEM UN LOGICIEL QUELCONQUE", -14.17, "Loisirs-culture-sport", 250.00, 250.00, "Main account")
      .add("04/08/2008", "SOLDE INITIAL", 1000.00, "To categorize", 264.17, 264.17, "Main account")
      .add("01/08/2008", "INTERNET", 15.00, "Internet", 0.00, -735.83, "Account n. 00001123")
      .check();

    // ici on a un 'bug' => [Test] n'est pas reimporté donc pas update.
    operations.openImportDialog()
      .setFilePath(getFile("money_export_standard.qif"))
      .acceptFile()
      .importSeries()
      .checkContains("[Test]")
      .setRecurring("[Test]")
      .checkNotContain("Alimentation")
      .validateAndFinishImport(0, 7, 0);

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
        {"2008", "Sep", 0.00, 30.00,},
        {"2008", "Oct", 0.00, 30.00,},
        {"2008", "Nov", 0.00, 30.00,},
      })
      .cancel();
    budgetView.recurring.checkSeriesNotPresent("Electricity");
    budgetView.recurring.checkSeriesPresent("Internet");
  }

  private void importStandard() throws IOException {
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
      .unset("[TEST]")
      .validateAndFinishImport(7, 0, 6);
  }

  @Test
  public void testStrictQifFile() throws Exception {
    operations.openImportDialog()
      .setFilePath(getFile("money_export_strict.qif"))
      .acceptFile()
      .createNewAccount("CIC", "Main account", "")
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
    mainAccounts.checkPosition("Main account", 835.83);
  }

  @Test
  public void testReImportWithOnMoreSeries() throws Exception {
    operations.openPreferences().setFutureMonthsCount(3).validate();
    importStandard();

    operations.openImportDialog()
      .setFilePath(getFile("money_export_standard_2.qif"))
      .acceptFile()
      .importSeries()
      .checkNotContain("Alimentation")
      .checkContains("Loisirs-culture-sport:Sport")
      .setVariable("Loisirs-culture-sport:Sport")
      .validateAndFinishImport();
    transactions.initContent()
      .add("20/08/2008", TransactionType.VIREMENT, "SPLIT COURSES QUELCONQUES", "", 0.00, "Alimentation / Epicerie")
      .add("20/08/2008", TransactionType.PRELEVEMENT, "SPLIT COURSES QUELCONQUES", "Motomag", -20.00, "Loisirs-culture-sport / Journaux")
      .add("20/08/2008", TransactionType.PRELEVEMENT, "SPLIT COURSES QUELCONQUES", "Courses quelconques", -30.00, "Alimentation / Epicerie")
      .add("20/08/2008", TransactionType.PRELEVEMENT, "ESSO", "", -100.00, "Auto-moto / Essence")
      .add("20/08/2008", TransactionType.PRELEVEMENT, "CREDIPLUS CREDIT PORSCHE", "", -100.00, "Auto-moto / Remboursement de pret auto-moto")
      .add("04/08/2008", TransactionType.PRELEVEMENT, "FNAC VELIZY CARTE 34609231 PAIEM UN AUTE LOGICIEL QUELCONQUE", "", -30.00, "Loisirs-culture-sport")
      .add("04/08/2008", TransactionType.PRELEVEMENT, "FNAC VELIZY CARTE 34609231 PAIEM UN LOGICIEL QUELCONQUE", "", -14.17, "Loisirs-culture-sport")
      .add("04/08/2008", TransactionType.VIREMENT, "SOLDE INITIAL", "", 1000.00)
      .check();
  }
}
