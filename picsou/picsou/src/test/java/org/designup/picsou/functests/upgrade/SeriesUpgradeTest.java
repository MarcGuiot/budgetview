package org.designup.picsou.functests.upgrade;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.globsframework.utils.Files;

public class SeriesUpgradeTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2014/12/15");
    super.setUp();
  }

  public void testSeries() throws Exception {
    operations.restore(Files.copyResourceToTmpFile(this, "/testbackups/upgrade_jar131_series_with_several_accounts.budgetview"));

    notifications.checkHidden();

    transactions.initAmountContent()
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
      .checkEditableTargetAccount("Main A")
      .checkAmount(100.00)
      .cancel();

    budgetView.variable.editSeries("Mono B")
      .checkEditableTargetAccount("Main B")
      .checkAmount(50.00)
      .cancel();

    transactions.showPlannedTransactions();
    transactions.initAmountContent()
      .add("15/12/2014", "Planned: Mono B", -50.00, "Mono B", 1950.00, 2900.00, "Main B")
      .add("15/12/2014", "Planned: Mono A", -50.00, "Mono A", 950.00, 2950.00, "Main A")
      .add("10/12/2014", "MULTI AB", -50.00, "Mono AB", 1000.00, 3000.00, "Main A")
      .add("08/12/2014", "MULTI AB", -150.00, "Mono AB", 2000.00, 3050.00, "Main B")
      .add("06/12/2014", "MULTI AB", -75.00, "Mono AB", 2150.00, 3200.00, "Main B")
      .add("05/12/2014", "MONO A", -50.00, "Mono A", 1050.00, 3275.00, "Main A")
      .check();

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

    views.selectData();
    transactions.initAmountContent()
      .add("11/12/2014", "SAVINGSB TO EXTERNAL", -200.00, "SavingsB to External", 3800.00, 6900.00, "SavingsB")
      .add("10/12/2014", "EXTERNAL TO SAVINGSA", 100.00, "External to SavingsA", 3100.00, 7100.00, "SavingsA")
      .add("08/12/2014", "REVERSE MAINAB TO SAVINGSA", 150.00, "MainAB to SavingsA (MainB)", 3000.00, 7000.00, "SavingsA")
      .add("08/12/2014", "TRANSFER MAINAB TO SAVINGSA", -150.00, "MainAB to SavingsA (MainB)", 2000.00, 3000.00, "MainB")
      .add("06/12/2014", "REVERSE MAINB TO SAVINGSB", 75.00, "MainB to SavingsB", 4000.00, 6850.00, "SavingsB")
      .add("06/12/2014", "TRANSFER MAINB TO SAVINGSB", -75.00, "MainB to SavingsB", 2150.00, 3150.00, "MainB")
      .add("05/12/2014", "REVERSE MAINAB TO SAVINGSA", 50.00, "MainAB to SavingsA (MainA)", 2850.00, 6775.00, "SavingsA")
      .add("05/12/2014", "TRANSFER MAINAB TO SAVINGSA", -50.00, "MainAB to SavingsA (MainA)", 1000.00, 3225.00, "MainA")
      .add("04/12/2014", "TRANSFER SAVINGSB TO MAINAB", -40.00, "SavingsB to MainAB (MainB)", 3925.00, 6725.00, "SavingsB")
      .add("04/12/2014", "REVERSE SAVINGSB TO MAINAB", 40.00, "SavingsB to MainAB (MainB)", 2225.00, 3275.00, "MainB")
      .add("03/12/2014", "TRANSFER SAVINGSB TO MAINAB", -30.00, "SavingsB to MainAB (MainA)", 3965.00, 6765.00, "SavingsB")
      .add("03/12/2014", "REVERSE SAVINGSB TO MAINAB", 30.00, "SavingsB to MainAB (MainA)", 1050.00, 3235.00, "MainA")
      .check();

    notifications.checkContent(
      "Series 'MainAB to SavingsA' has been splitted because transactions from several main accounts were assigned to it.",
      "Series 'SavingsB to MainAB' has been splitted because transactions from several main accounts were assigned to it.");


    budgetView.transfer
      .checkContent("| SavingsB to External       | 200.00  | To define |\n" +
                    "| MainAB to SavingsA (MainB) | 150.00  | 50.00     |\n" +
                    "| External to SavingsA       | +100.00 | +100.00   |\n" +
                    "| MainB to SavingsB          | 75.00   | 75.00     |\n" +
                    "| MainAB to SavingsA (MainA) | 50.00   | 50.00     |\n" +
                    "| SavingsB to MainAB (MainB) | +40.00  | +25.00    |\n" +
                    "| SavingsB to MainAB (MainA) | +30.00  | +25.00    |")
      .checkGroupToggleNotShown("MainB to SavingsB")
      .checkGroupToggleNotShown("MainAB to SavingsA (MainA)");
    budgetView.transfer
      .editSeries("MainB to SavingsB")
      .checkFromAccount("MainB")
      .checkToAccount("SavingsB")
      .cancel();
    budgetView.transfer
      .editSeries("MainAB to SavingsA (MainB)")
      .checkFromAccount("MainB")
      .checkToAccount("SavingsA")
      .cancel();
    budgetView.transfer
      .editSeries("MainAB to SavingsA (MainA)")
      .checkFromAccount("MainA")
      .checkToAccount("SavingsA")
      .cancel();

    savingsAccounts.select("SavingsA");
    budgetView.transfer
      .checkContent("| MainAB to SavingsA (MainB) | +150.00 | +50.00  |\n" +
                    "| External to SavingsA       | +100.00 | +100.00 |\n" +
                    "| MainAB to SavingsA (MainA) | +50.00  | +50.00  |");
    budgetView.transfer
      .editSeries("External to SavingsA")
      .checkFromAccount("External account")
      .checkToAccount("SavingsA")
      .cancel();
    budgetView.transfer
      .editSeries("MainAB to SavingsA (MainB)")
      .checkFromAccount("MainB")
      .checkToAccount("SavingsA")
      .cancel();
    budgetView.transfer
      .editSeries("MainAB to SavingsA (MainA)")
      .checkFromAccount("MainA")
      .checkToAccount("SavingsA")
      .cancel();

    savingsAccounts.select("SavingsB");
    budgetView.transfer
      .checkContent("| SavingsB to External       | 200.00 | To define |\n" +
                    "| MainB to SavingsB          | +75.00 | +75.00    |\n" +
                    "| SavingsB to MainAB (MainB) | 40.00  | 25.00     |\n" +
                    "| SavingsB to MainAB (MainA) | 30.00  | 25.00     |");
    budgetView.transfer
      .editSeries("SavingsB to External")
      .checkFromAccount("SavingsB")
      .checkToAccount("External account")
      .cancel();
  }

  public void testTransfersWithNoTransactions() throws Exception {

    operations.restore(Files.copyResourceToTmpFile(this, "/testbackups/upgrade_jar131_series_transfers_no_transactions.budgetview"));

    timeline.checkSelection("2014/12");
    budgetView.transfer
      .checkContent("| SavingsB to Main | 0.00 | +150.00 |\n" +
                    "| Main to SavingsA | 0.00 | 100.00  |");

    notifications.checkContent(
      "Series 'Main to SavingsA' is now using account 'MainA'.",
      "Series 'SavingsB to Main' is now using account 'MainA'."
    );

    transactions.showPlannedTransactions()
      .sortByEnvelope()
      .initAmountContent()
      .add("15/12/2014", "Planned: Main to SavingsA", -100.00, "Main to SavingsA", 1100.00, 3100.00, "MainA")
      .add("15/12/2014", "Planned: Main to SavingsA", 100.00, "Main to SavingsA", 10100.00, 30100.00, "SavingsA")
      .add("15/12/2014", "Planned: SavingsB to Main", 150.00, "SavingsB to Main", 1200.00, 3200.00, "MainA")
      .add("15/12/2014", "Planned: SavingsB to Main", -150.00, "SavingsB to Main", 19850.00, 29950.00, "SavingsB")
      .check();

    mainAccounts.checkEndOfMonthPosition("MainA", 1100.00);
    mainAccounts.checkEndOfMonthPosition("MainB", 2000.00);
  }

  public void testNoMessagesDisplayedWhenConvertingASavingsSeriesWithNoTransactionsAndASingleMainAccount() throws Exception {
    operations.restore(Files.copyResourceToTmpFile(this, "/testbackups/upgrade_jar131_series_transfers_no_transactions_one_main_account.budgetview"));

    budgetView.transfer
      .checkContent("| Main to SavingsA | 0.00 | 500.00 |");

    notifications.checkHidden();

    transactions.showPlannedTransactions();
    transactions.initAmountContent()
      .add("15/12/2014", "Planned: Main to SavingsA", 500.00, "Main to SavingsA", 2500.00, 5500.00, "SavingsA")
      .add("15/12/2014", "Planned: Main to SavingsA", -500.00, "Main to SavingsA", 500.00, 500.00, "MainA")
      .check();

    budgetView.transfer
      .editSeries("Main to SavingsA")
      .checkFromAccount("MainA")
      .checkToAccount("SavingsA")
      .cancel();
  }
}