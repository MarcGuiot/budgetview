package org.designup.picsou.functests.upgrade;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.globsframework.utils.Files;

import java.io.File;

public class SeriesUpgradeTest extends LoggedInFunctionalTestCase {

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
    String fileName = "tmp/series_upgrade_savings.ofx";
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