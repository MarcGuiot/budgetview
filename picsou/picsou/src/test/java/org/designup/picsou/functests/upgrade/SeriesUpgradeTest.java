package org.designup.picsou.functests.upgrade;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Files;

import java.io.File;

public class SeriesUpgradeTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2014/12/15");
    super.setUp();
  }

  public void testSeries() throws Exception {
    operations.restore(Files.copyResourceToTmpFile(this, "/testbackups/upgrade_jar131_series_with_several_accounts.budgetview"));

    transactions.showPlannedTransactions();
    transactions.initAmountContent()
      .add("15/12/2014", "Planned: Mono B", -50.00, "Mono B", 1950.00, 2900.00, "Main B")
      .add("15/12/2014", "Planned: Mono A", -50.00, "Mono A", 950.00, 2950.00, "Main A")
      .add("10/12/2014", "MULTI AB", -50.00, "Mono AB", 1000.00, 3000.00, "Main A")
      .add("08/12/2014", "MULTI AB", -150.00, "Mono AB", 2000.00, 3050.00, "Main B")
      .add("06/12/2014", "MULTI AB", -75.00, "Mono AB", 2150.00, 3200.00, "Main B")
      .add("05/12/2014", "MONO A", -50.00, "Mono A", 1050.00, 3275.00, "Main A")
      .check();

    budgetView.variable
      .checkContent("| Mono AB | 275.00 | 200.00 |\n" +
                    "| Mono A  | 50.00  | 100.00 |\n" +
                    "| Mono B  | 0.00   | 50.00  |")
      .checkGroupToggleNotShown("Mono AB")
      .checkGroupToggleNotShown("Mono A")
      .checkGroupToggleNotShown("Mono B");

    budgetView.variable.editSeries("Mono AB")
      .checkReadOnlyTargetAccount("Main accounts")
      .checkAmount(200.00)
      .cancel();

    budgetView.variable.editSeries("Mono A")
      .checkReadOnlyTargetAccount("Main A")
      .checkAmount(100.00)
      .cancel();

    budgetView.variable.editSeries("Mono B")
      .checkReadOnlyTargetAccount("Main B")
      .checkAmount(50.00)
      .cancel();

    timeline.selectMonth(201501);
    transactions.initAmountContent()
      .add("15/01/2015", "Planned: Mono B", -50.00, "Mono B", 1736.36, 2550.00, "Main B")
      .add("15/01/2015", "Planned: Mono A", -100.00, "Mono A", 813.64, 2600.00, "Main A")
      .add("12/01/2015", "Planned: Mono AB", -31.64, "Mono AB", 1786.36, 2700.00, "Main B")
      .add("08/01/2015", "Planned: Mono AB", -132.00, "Mono AB", 1818.00, 2731.64, "Main B")
      .add("08/01/2015", "Planned: Mono AB", -36.36, "Mono AB", 913.64, 2863.64, "Main A")
      .check();
  }

  public void testTransfersWithTransactions() throws Exception {
    operations.restore(Files.copyResourceToTmpFile(this, "/testbackups/upgrade_jar131_series_with_savings_transfers.budgetview"));

    timeline.checkSelection("201412/01");

    budgetView.recurring
      .checkContent("| Energies | 25.00 | 25.00 |\n")
      .checkGroupToggleNotShown("Energies");

    transactions.initContent()
      .dumpCode();
  }

  public void testTransfersWithNoTransactions() throws Exception {
    operations.restore(Files.copyResourceToTmpFile(this, "/testbackups/upgrade_jar131_series_transfers_no_transactions.budgetview"));

  }

  public static void main(String... args) throws Exception {
    createSeriesFile("tmp/series_upgrade.ofx");
    createTransfersFile("tmp/series_upgrade_transfers.ofx");
  }

  public static void createSeriesFile(String fileName) {
    OfxBuilder.init(fileName)
      .addBankAccount("11111", 1000.00, "2014/12/15") // MainA
      .addTransaction("2014/12/05", -50.00, "MONO A")
      .addTransaction("2014/12/10", -50.00, "MULTI AB")
      .addTransaction("2014/11/03", -30.00, "MONO A")
      .addTransaction("2014/11/15", 30.00, "MONO A")

      .addBankAccount("22222", 2000.00, "2014/12/15") // MainB
      .addTransaction("2014/12/08", -150.00, "MULTI AB")
      .addTransaction("2014/12/06", -75.00, "MULTI AB")
      .addTransaction("2014/11/15", -45.00, "MONO B")

      .save();

    System.out.println("SeriesUpgradeTest: DONE - " + new File(fileName).getAbsolutePath());
  }

  public static void createTransfersFile(String fileName) {
    OfxBuilder.init(fileName)
      .addBankAccount("11111", 1000.00, "2014/12/15") // MainA
      .addTransaction("2014/12/05", -50.00, "TRANSFER MAINAB TO SAVINGSA")
      .addTransaction("2014/12/03", 30.00, "REVERSE SAVINGSB TO MAINAB")

      .addBankAccount("22222", 2000.00, "2014/12/15") // MainB
      .addTransaction("2014/12/08", -150.00, "TRANSFER MAINAB TO SAVINGSA")
      .addTransaction("2014/12/06", -75.00, "TRANSFER MAINB TO SAVINGSB")
      .addTransaction("2014/12/04", 40.00, "REVERSE SAVINGSB TO MAINAB")

      .addBankAccount("33333", 3000.00, "2014/12/15") // SavingsA
      .addTransaction("2014/12/05", 50.00, "REVERSE MAINAB TO SAVINGSA")
      .addTransaction("2014/12/08", 150.00, "REVERSE MAINAB TO SAVINGSA")
      .addTransaction("2014/12/03", -30.00, "TRANSFER SAVINGSB TO MAINAB")
      .addTransaction("2014/12/04", -40.00, "TRANSFER SAVINGSB TO MAINAB")

      .addBankAccount("44444", 4000.00, "2014/12/15") // SavingsB
      .addTransaction("2014/12/06", 75.00, "REVERSE MAINB TO SAVINGSB")

      .save();

    System.out.println("SeriesUpgradeTest: DONE - " + new File(fileName).getAbsolutePath());
  }
}