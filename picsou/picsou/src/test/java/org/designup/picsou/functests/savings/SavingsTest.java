package org.designup.picsou.functests.savings;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.model.TransactionType;

public class SavingsTest extends LoggedInFunctionalTestCase {

  public void testCreateSavingsSeries() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .addTransaction("2008/07/10", -100.00, "Virement")
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();

    OfxBuilder.init(this)
      .addBankAccount("000222", 1300.00, "2008/08/30")
      .addTransaction("2008/06/10", 100.00, "Virement")
      .addTransaction("2008/07/10", 100.00, "Virement")
      .addTransaction("2008/08/10", 100.00, "Virement")
      .load();
    mainAccounts.setAsSavings("Account n. 000222", "Epargne LCL");

    timeline.selectAll();
    categorization
      .selectTransactions("Virement")
      .selectTransfers().createSeries()
      .setName("Virt Epargne")
      .setFromAccount("Account n. 00001123")
      .checkFromContentEquals("Account n. 00001123")
      .setToAccount("Epargne LCL")
      .checkToContentEquals("Epargne LCL")
      .validate();

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.transfer.alignAndPropagate("Virt Epargne");

    timeline.selectAll();
    budgetView.transfer
      .checkSeries("Virt Epargne", -300.00, -500.00);

    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: Virt Epargne", 100.00, "Virt Epargne", 1500.00, 1500.00, "Epargne LCL")
      .add("11/10/2008", "Planned: Virt Epargne", -100.00, "Virt Epargne", -200.00, -200.00, "Account n. 00001123")
      .add("11/09/2008", "Planned: Virt Epargne", 100.00, "Virt Epargne", 1400.00, 1400.00, "Epargne LCL")
      .add("11/09/2008", "Planned: Virt Epargne", -100.00, "Virt Epargne", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", 100.00, "Virt Epargne", 1300.00, 1300.00, "Epargne LCL")
      .add("10/08/2008", "VIREMENT", -100.00, "Virt Epargne", 0.00, 0.00, "Account n. 00001123")
      .add("10/07/2008", "VIREMENT", 100.00, "Virt Epargne", 1200.00, 1200.00, "Epargne LCL")
      .add("10/07/2008", "VIREMENT", -100.00, "Virt Epargne", 100.00, 100.00, "Account n. 00001123")
      .add("10/06/2008", "VIREMENT", 100.00, "Virt Epargne", 1100.00, 1100.00, "Epargne LCL")
      .add("10/06/2008", "VIREMENT", -100.00, "Virt Epargne", 200.00, 200.00, "Account n. 00001123")
      .check();

    timeline.selectMonth("2008/08");
    savingsAccounts.checkEndOfMonthPosition("Epargne LCL", 1300.00);

    timeline.selectMonth("2008/09");
    savingsAccounts.checkEndOfMonthPosition("Epargne LCL", 1400.00);

    timeline.selectMonth("2008/10");
    savingsAccounts.checkEndOfMonthPosition("Epargne LCL", 1500.00);

    timeline.selectMonth("2008/06");
    savingsAccounts.select("Epargne LCL");
    budgetView.transfer
      .checkContent("| Virt Epargne | +100.00 | +100.00 |")
      .checkTotalAmounts("+100.00", "+100.00");

    mainAccounts.select("Account n. 00001123");
    budgetView.transfer
      .checkTotalAmounts("100.00", "100.00")
      .checkContent("| Virt Epargne | 100.00 | 100.00 |");

    uncategorized.checkNotShown();
  }

  public void testCreateSavingsSeriesAndPayFromSavings() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .addTransaction("2008/07/10", -100.00, "Virement")
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();

    OfxBuilder.init(this)
      .addBankAccount("000222", 1300.00, "2008/08/30")
      .addTransaction("2008/06/10", 100.00, "Virement")
      .addTransaction("2008/07/10", 100.00, "Virement")
      .addTransaction("2008/08/10", 100.00, "Virement")
      .load();
    mainAccounts.setAsSavings("Account n. 000222", "Epargne LCL");

    categorization
      .selectTransactions("Virement")
      .selectTransfers().createSeries()
      .setName("Virt Epargne")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Epargne LCL")
      .validate();
    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.transfer.alignAndPropagate("Virt Epargne");

    budgetView.transfer.createSeries()
      .setName("Achat Tele")
      .setFromAccount("Epargne LCL")
      .setToAccount("Account n. 00001123")
      .selectMonth(200810)
      .setAmount(300)
      .validate();

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: Achat Tele", -300.00, "Achat Tele", 1200.00, 1200.00, "Epargne LCL")
      .add("11/10/2008", "Planned: Virt Epargne", 100.00, "Virt Epargne", 1500.00, 1500.00, "Epargne LCL")
      .add("11/10/2008", "Planned: Virt Epargne", -100.00, "Virt Epargne", 100.00, 100.00, "Account n. 00001123")
      .add("11/10/2008", "Planned: Achat Tele", 300.00, "Achat Tele", 200.00, 200.00, "Account n. 00001123")
      .add("11/09/2008", "Planned: Virt Epargne", 100.00, "Virt Epargne", 1400.00, 1400.00, "Epargne LCL")
      .add("11/09/2008", "Planned: Virt Epargne", -100.00, "Virt Epargne", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", 100.00, "Virt Epargne", 1300.00, 1300.00, "Epargne LCL")
      .add("10/08/2008", "VIREMENT", -100.00, "Virt Epargne", 0.00, 0.00, "Account n. 00001123")
      .add("10/07/2008", "VIREMENT", 100.00, "Virt Epargne", 1200.00, 1200.00, "Epargne LCL")
      .add("10/07/2008", "VIREMENT", -100.00, "Virt Epargne", 100.00, 100.00, "Account n. 00001123")
      .add("10/06/2008", "VIREMENT", 100.00, "Virt Epargne", 1100.00, 1100.00, "Epargne LCL")
      .add("10/06/2008", "VIREMENT", -100.00, "Virt Epargne", 200.00, 200.00, "Account n. 00001123")
      .check();

    timeline.selectMonth("2008/10");
    savingsAccounts.checkEndOfMonthPosition("Epargne LCL", 1200);
    budgetView.transfer.checkSeries("Achat Tele", 0, 300);
    budgetView.transfer.checkTotalAmounts("0.00", "+200.00");

    timeline.selectMonth("2008/06");
    savingsAccounts.select("Epargne LCL");
    budgetView.transfer
      .checkContent("| Virt Epargne | +100.00 | +100.00 |\n" +
                    "| Achat Tele   | 0.00    | 0.00    |")
      .checkTotalAmounts("+100.00", "+100.00");
  }

  public void testSavingsAccountFilledFromExternalAccountBalance() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addBankAccount("000111", 1200.00, "2008/11/11")
      .addTransaction("2008/08/11", 200.00, "VIRT EPARGNE")
      .load();

    mainAccounts.edit("Account n. 000111")
      .setAsSavings()
      .setName("Epargne LCL")
      .selectBank("LCL")
      .validate();

    categorization.selectTransaction("VIRT EPARGNE")
      .selectTransfers()
      .createSeries()
      .setName("Virt Epargne")
      .setFromAccount("External Account")
      .setToAccount("Epargne LCL")
      .selectAllMonths()
      .setAmount("200")
      .validate();

    budgetView.transfer
      .createSeries()
      .setName("Travaux")
      .setFromAccount("Epargne LCL")
      .setToAccount("External Account")
      .selectMonth(200810)
      .setAmount("400")
      .validate();

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: Travaux", -400.00, "Travaux", 1200.00, 1200.00, "Epargne LCL")
      .add("11/10/2008", "Planned: Virt Epargne", 200.00, "Virt Epargne", 1600.00, 1600.00, "Epargne LCL")
      .add("11/09/2008", "Planned: Virt Epargne", 200.00, "Virt Epargne", 1400.00, 1400.00, "Epargne LCL")
      .add("11/08/2008", "VIRT EPARGNE", 200.00, "Virt Epargne", 1200.00, 1200.00, "Epargne LCL")
      .check();

    timeline.selectMonth("2008/10");
    budgetView.transfer
      .checkNoSeriesShown() // Only visible when selecting a savings account
      .checkTotalAmounts(0, 0);

    savingsAccounts.select("Epargne LCL");
    savingsAccounts.checkEndOfMonthPosition("Epargne LCL", 1200);
    budgetView.transfer
      .checkContent("| Travaux      | 0.00 | 400.00  |\n" +
                    "| Virt Epargne | 0.00 | +200.00 |")
      .checkTotalAmounts("0.00", "200.00");
    budgetView.transfer
      .checkGaugeTooltip("Virt Epargne", "Il vous reste <b>200.00</b> à virer")
      .checkGaugeTooltip("Travaux", "Il vous reste <b>400.00</b> à virer");

    uncategorized.checkNotShown();
  }

  public void testCreateSavingsSeriesAndAssociateLaterToAccount() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Vers epargne")
      .addTransaction("2008/07/10", -100.00, "Vers epargne")
      .addTransaction("2008/08/10", -100.00, "Vers epargne")
      .load();

    categorization
      .selectTransactions("Vers epargne")
      .selectTransfers()
      .createSeries()
      .setName("Virt Epargne")
      .checkFromContentEquals("Account n. 00001123")
      .setFromAccount("Account n. 00001123")
      .checkToContentEquals("External account")
      .setToAccount("External account")
      .validate();

    timeline.selectMonth("2008/06");
    budgetView.transfer.alignAndPropagate("Virt Epargne");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: Virt Epargne", -100.00, "Virt Epargne", -200.00, -200.00, "Account n. 00001123")
      .add("11/09/2008", "Planned: Virt Epargne", -100.00, "Virt Epargne", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "VERS EPARGNE", -100.00, "Virt Epargne", 0.00, 0.00, "Account n. 00001123")
      .add("10/07/2008", "VERS EPARGNE", -100.00, "Virt Epargne", 100.00, 100.00, "Account n. 00001123")
      .add("10/06/2008", "VERS EPARGNE", -100.00, "Virt Epargne", 200.00, 200.00, "Account n. 00001123")
      .check();

    OfxBuilder.init(this)
      .addBankAccount("000222", 1300.00, "2008/08/30")
      .addTransaction("2008/06/10", 100.00, "De courant")
      .addTransaction("2008/07/10", 100.00, "De courant")
      .addTransaction("2008/08/10", 100.00, "De courant")
      .load();
    mainAccounts.setAsSavings("Account n. 000222", "Epargne");

    budgetView.transfer.editSeries("Virt Epargne")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Epargne")
      .validate();

    categorization.initContent()
      .add("10/06/2008", "", "DE COURANT", 100.00)
      .add("10/07/2008", "", "DE COURANT", 100.00)
      .add("10/08/2008", "", "DE COURANT", 100.00)
      .add("10/06/2008", "", "VERS EPARGNE", -100.00)
      .add("10/07/2008", "", "VERS EPARGNE", -100.00)
      .add("10/08/2008", "", "VERS EPARGNE", -100.00)
      .check();

    uncategorized.checkAmountAndTransactions(200.00, "| 10/08/2008 |  | DE COURANT   | 100.00  |\n" +
                                                     "| 10/08/2008 |  | VERS EPARGNE | -100.00 |");

    categorization.showAllTransactions();
    categorization.selectTransactions("VERS EPARGNE")
      .selectTransfers()
      .checkContainsSeries("Virt Epargne")
      .selectSeries("Virt Epargne");
    categorization.selectTransactions("DE COURANT")
      .selectTransfers()
      .checkContainsSeries("Virt Epargne")
      .selectSeries("Virt Epargne");

    timeline.selectMonth("2008/06");
    budgetView.transfer.alignAndPropagate("Virt Epargne");

    timeline.selectAll();
    transactions.initAmountContent()
      .add("11/10/2008", "Planned: Virt Epargne", 100.00, "Virt Epargne", 1500.00, 1500.00, "Epargne")
      .add("11/10/2008", "Planned: Virt Epargne", -100.00, "Virt Epargne", -200.00, -200.00, "Account n. 00001123")
      .add("11/09/2008", "Planned: Virt Epargne", 100.00, "Virt Epargne", 1400.00, 1400.00, "Epargne")
      .add("11/09/2008", "Planned: Virt Epargne", -100.00, "Virt Epargne", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "DE COURANT", 100.00, "Virt Epargne", 1300.00, 1300.00, "Epargne")
      .add("10/08/2008", "VERS EPARGNE", -100.00, "Virt Epargne", 0.00, 0.00, "Account n. 00001123")
      .add("10/07/2008", "DE COURANT", 100.00, "Virt Epargne", 1200.00, 1200.00, "Epargne")
      .add("10/07/2008", "VERS EPARGNE", -100.00, "Virt Epargne", 100.00, 100.00, "Account n. 00001123")
      .add("10/06/2008", "DE COURANT", 100.00, "Virt Epargne", 1100.00, 1100.00, "Epargne")
      .add("10/06/2008", "VERS EPARGNE", -100.00, "Virt Epargne", 200.00, 200.00, "Account n. 00001123")
      .check();

    uncategorized.checkNotShown();
  }

  public void testCreateSavingsSeriesAndAssociateLaterToAnotherAccount() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .addTransaction("2008/07/10", -100.00, "Virement")
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    timeline.selectAll();

    OfxBuilder.init(this)
      .addBankAccount("000222", 1300.00, "2008/08/30")
      .addTransaction("2008/06/10", 100.00, "Virt LCL")
      .addTransaction("2008/07/10", 100.00, "Virt LCL")
      .addTransaction("2008/08/10", 100.00, "Virt LCL")
      .load();
    mainAccounts.setAsSavings("Account n. 000222", "Epargne LCL");

    categorization
      .selectTransactions("Virement")
      .selectTransfers().createSeries()
      .setFromAccount("Account n. 00001123")
      .setToAccount("Epargne LCL")
      .setName("Virt Epargne")
      .validate();
    categorization.setTransfer("VIRT LCL", "Virt Epargne");

    timeline.selectMonth("2008/06");
    budgetView.transfer.alignAndPropagate("Virt Epargne");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: Virt Epargne", 100.00, "Virt Epargne", 1500.00, 1500.00, "Epargne LCL")
      .add("11/10/2008", "Planned: Virt Epargne", -100.00, "Virt Epargne", -200.00, -200.00, "Account n. 00001123")
      .add("11/09/2008", "Planned: Virt Epargne", 100.00, "Virt Epargne", 1400.00, 1400.00, "Epargne LCL")
      .add("11/09/2008", "Planned: Virt Epargne", -100.00, "Virt Epargne", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "VIRT LCL", 100.00, "Virt Epargne", 1300.00, 1300.00, "Epargne LCL")
      .add("10/08/2008", "VIREMENT", -100.00, "Virt Epargne", 0.00, 0.00, "Account n. 00001123")
      .add("10/07/2008", "VIRT LCL", 100.00, "Virt Epargne", 1200.00, 1200.00, "Epargne LCL")
      .add("10/07/2008", "VIREMENT", -100.00, "Virt Epargne", 100.00, 100.00, "Account n. 00001123")
      .add("10/06/2008", "VIRT LCL", 100.00, "Virt Epargne", 1100.00, 1100.00, "Epargne LCL")
      .add("10/06/2008", "VIREMENT", -100.00, "Virt Epargne", 200.00, 200.00, "Account n. 00001123")
      .check();

    uncategorized.checkNotShown();

    OfxBuilder.init(this)
      .addBankAccount("000333", 100.00, "2008/08/30")
      .addTransaction("2008/06/10", 100.00, "Virt CIC")
      .addTransaction("2008/07/10", 100.00, "Virt CIC")
      .addTransaction("2008/08/10", 100.00, "Virt CIC")
      .load();
    mainAccounts.setAsSavings("Account n. 000333", "Epargne CIC");

    categorization.selectTransactions("VIRT LCL")
      .setUncategorized();

    uncategorized.checkAmountAndTransactions(200.00, "| 10/08/2008 |  | VIRT CIC | 100.00 |\n" +
                                                     "| 10/08/2008 |  | VIRT LCL | 100.00 |");

    budgetView.transfer.editSeries("Virt Epargne")
      .setToAccount("Epargne CIC")
      .validate();

    uncategorized.checkAmountAndTransactions(300.00, "| 10/08/2008 |  | VIREMENT | -100.00 |\n" +
                                                     "| 10/08/2008 |  | VIRT CIC | 100.00  |\n" +
                                                     "| 10/08/2008 |  | VIRT LCL | 100.00  |");

    timeline.selectAll();
    transactions.initAmountContent()
      .add("11/10/2008", "Planned: Virt Epargne", 100.00, "Virt Epargne", 400.00, 1700.00, "Epargne CIC")
      .add("11/10/2008", "Planned: Virt Epargne", -100.00, "Virt Epargne", -300.00, -300.00, "Account n. 00001123")
      .add("11/09/2008", "Planned: Virt Epargne", 100.00, "Virt Epargne", 300.00, 1600.00, "Epargne CIC")
      .add("11/09/2008", "Planned: Virt Epargne", -100.00, "Virt Epargne", -200.00, -200.00, "Account n. 00001123")
      .add("11/08/2008", "Planned: Virt Epargne", 100.00, "Virt Epargne", 200.00, 1500.00, "Epargne CIC")
      .add("11/08/2008", "Planned: Virt Epargne", -100.00, "Virt Epargne", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "VIRT CIC", 100.00, "To categorize", 100.00, 1400.00, "Epargne CIC")
      .add("10/08/2008", "VIRT LCL", 100.00, "To categorize", 1300.00, 1300.00, "Epargne LCL")
      .add("10/08/2008", "VIREMENT", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .add("10/07/2008", "VIRT CIC", 100.00, "To categorize", 0.00, 1200.00, "Epargne CIC")
      .add("10/07/2008", "VIRT LCL", 100.00, "To categorize", 1200.00, 1100.00, "Epargne LCL")
      .add("10/07/2008", "VIREMENT", -100.00, "To categorize", 100.00, 100.00, "Account n. 00001123")
      .add("10/06/2008", "VIRT CIC", 100.00, "To categorize", -100.00, 1000.00, "Epargne CIC")
      .add("10/06/2008", "VIRT LCL", 100.00, "To categorize", 1100.00, 900.00, "Epargne LCL")
      .add("10/06/2008", "VIREMENT", -100.00, "To categorize", 200.00, 200.00, "Account n. 00001123")
      .check();

    categorization.showAllTransactions();
    categorization.setTransfer("VIREMENT", "Virt Epargne");
    categorization.setTransfer("VIRT CIC", "Virt Epargne");
    transactions.initAmountContent()
      .add("11/10/2008", "Planned: Virt Epargne", 100.00, "Virt Epargne", 300.00, 1600.00, "Epargne CIC")
      .add("11/10/2008", "Planned: Virt Epargne", -100.00, "Virt Epargne", -200.00, -200.00, "Account n. 00001123")
      .add("11/09/2008", "Planned: Virt Epargne", 100.00, "Virt Epargne", 200.00, 1500.00, "Epargne CIC")
      .add("11/09/2008", "Planned: Virt Epargne", -100.00, "Virt Epargne", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "VIRT CIC", 100.00, "Virt Epargne", 100.00, 1400.00, "Epargne CIC")
      .add("10/08/2008", "VIRT LCL", 100.00, "To categorize", 1300.00, 1300.00, "Epargne LCL")
      .add("10/08/2008", "VIREMENT", -100.00, "Virt Epargne", 0.00, 0.00, "Account n. 00001123")
      .add("10/07/2008", "VIRT CIC", 100.00, "Virt Epargne", 0.00, 1200.00, "Epargne CIC")
      .add("10/07/2008", "VIRT LCL", 100.00, "To categorize", 1200.00, 1100.00, "Epargne LCL")
      .add("10/07/2008", "VIREMENT", -100.00, "Virt Epargne", 100.00, 100.00, "Account n. 00001123")
      .add("10/06/2008", "VIRT CIC", 100.00, "Virt Epargne", -100.00, 1000.00, "Epargne CIC")
      .add("10/06/2008", "VIRT LCL", 100.00, "To categorize", 1100.00, 900.00, "Epargne LCL")
      .add("10/06/2008", "VIREMENT", -100.00, "Virt Epargne", 200.00, 200.00, "Account n. 00001123")
      .check();
  }

  public void testSplitPreviouslyCategorizedInSavings() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    timeline.selectAll();

    accounts.createSavingsAccount("Virt Epargne", 1000.);
    categorization
      .selectTransactions("Virement")
      .selectTransfers().createSeries()
      .setName("Virt Epargne")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Virt Epargne")
      .validate();

    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("10/08/2008", "VIREMENT", -100.00, "Virt Epargne", 0.00, 0.00, "Account n. 00001123")
      .check();

    categorization.selectTransactions("Virement");
    transactionDetails.split("50", "Comportement impossible?");

    uncategorized.checkAmount(50.00);
    categorization.checkTableContent("| 10/08/2008 | Virt Epargne | VIREMENT | -50.00 |\n" +
                                     "| 10/08/2008 |              | VIREMENT | -50.00 |");

    categorization.showAllTransactions();
    categorization.selectVariable().selectNewSeries("Occasional");
    transactions
      .initAmountContent()
      .add("10/08/2008", "VIREMENT", -50.00, "Virt Epargne", 0.00, 0.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", -50.00, "Occasional", 50.00, 50.00, "Account n. 00001123")
      .check();

    uncategorized.checkNotShown();
  }

  public void testExternalToSavingsWithDate() throws Exception {
    // force creation of months in the past
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "FNAC")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addBankAccount("000111", 1300.00, "2008/08/05")
      .addTransaction("2008/08/05", 300.00, "CAF")
      .load();

    mainAccounts.edit("Account n. 000111")
      .setAsSavings()
      .setName("Virt Epargne")
      .selectBank("LCL")
      .validate();

    categorization.selectTransaction("CAF")
      .selectTransfers()
      .createSeries()
      .setName("CAF")
      .setFromAccount("External account")
      .setToAccount("Virt Epargne")
      .setStartDate(200808)
      .selectAllMonths()
      .setAmount("300")
      .setDay("5")
      .validate();

    timeline.selectMonth("2008/06");
    savingsAccounts.checkEndOfMonthPosition("Virt Epargne", 1000);
    savingsAccounts.checkReferencePosition(1300, "2008/08/05");

    timeline.selectMonth("2008/08");
    savingsAccounts.checkEndOfMonthPosition("Virt Epargne", 1300);
    savingsAccounts.checkReferencePosition(1300, "2008/08/05");

    timeline.selectMonth("2008/09");
    savingsAccounts.checkEndOfMonthPosition("Virt Epargne", 1600);

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("04/10/2008", "Planned: CAF", 300.00, "CAF", 1900.00, 1900.00, "Virt Epargne")
      .add("04/09/2008", "Planned: CAF", 300.00, "CAF", 1600.00, 1600.00, "Virt Epargne")
      .add("05/08/2008", "CAF", 300.00, "CAF", 1300.00, 1300.00, "Virt Epargne")
      .add("10/06/2008", "FNAC", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .check();

    uncategorized.checkAmountAndTransactions(100.00, "| 10/06/2008 |  | FNAC | -100.00 |");

    timeline.selectMonth("2008/06");
    budgetView.transfer.checkTotalAmounts(0, 0);

    timeline.selectMonth("2008/10");
    savingsAccounts.select("Virt Epargne");
    budgetView.transfer
      .checkContent("| CAF | 0.00 | +300.00 |")
      .checkTotalAmounts("0.00", "+300.00");

    timeline.selectMonth("2008/08");
    budgetView.transfer
      .checkContent("| CAF | +300.00 | +300.00 |")
      .checkTotalAmounts("+300.00", "+300.00");

    budgetView.transfer.editSeries("CAF")
      .selectMonth(200808)
      .setAmount(0)
      .selectMonth(200809)
      .setAmount(0)
      .validate();

    // back to normal to see if dateChooser is hidden

    budgetView.transfer.editSeries("CAF")
      .setFromAccount("Account n. 00001123")
      .checkDateChooserIsHidden()
      .validate();

    timeline.selectAll();
    savingsAccounts.unselect("Virt Epargne");
    transactions.initAmountContent()
      .add("11/10/2008", "Planned: CAF", 300.00, "CAF", 1600.00, 1600.00, "Virt Epargne")
      .add("11/10/2008", "Planned: CAF", -300.00, "CAF", -300.00, -300.00, "Account n. 00001123")
      .add("05/08/2008", "CAF", 300.00, "To categorize", 1300.00, 1300.00, "Virt Epargne")
      .add("10/06/2008", "FNAC", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .check();

    mainAccounts.select("Account n. 00001123");
    budgetView.transfer.editSeries("CAF")
      .checkDateChooserIsHidden()
      .cancel();

    uncategorized.checkAmountAndTransactions(100.00, "| 10/06/2008 |  | FNAC | -100.00 |");
  }

  // ==> test de l'effet de suppression de transaction référencée dans account
  public void testUpdateAccountOnSeriesChange() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addBankAccount("00001123", 0.00, "2008/08/05")
      .addTransaction("2008/08/10", -100.00, "Caf")
      .load();
    mainAccounts.edit("Account n. 00001123").setName("Main1").validate();

    OfxBuilder.init(this)
      .addBankAccount("000222", 1100.00, "2008/08/05")
      .addTransaction("2008/08/10", 100.00, "Caf")
      .load();
    mainAccounts.edit("Account n. 000222")
      .setAsSavings()
      .setName("Savings1")
      .selectBank("LCL")
      .validate();

    budgetView.transfer
      .createSeries()
      .setName("CAF")
      .setFromAccount("External account")
      .setToAccount("Savings1")
      .selectAllMonths()
      .setAmount("100")
      .setDay("5")
      .validate();

    timeline.selectMonth("2008/08");
    savingsAccounts.checkEndOfMonthPosition("Savings1", 1200);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkEndOfMonthPosition("Savings1", 1300);

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: CAF", 100.00, "CAF", 1400.00, 1400.00, "Savings1")
      .add("11/09/2008", "Planned: CAF", 100.00, "CAF", 1300.00, 1300.00, "Savings1")
      .add("11/08/2008", "Planned: CAF", 100.00, "CAF", 1200.00, 1200.00, "Savings1")
      .add("10/08/2008", "CAF", 100.00, "To categorize", 1100.00, 1100.00, "Savings1")
      .add("10/08/2008", "CAF", -100.00, "To categorize", 0.00, 0.00, "Main1")
      .check();

    savingsAccounts.select("Savings1");
    budgetView.transfer
      .editSeries("CAF")
      .setFromAccount("Main1")
      .validate();

    categorization.setTransfer("Caf", "CAF");

    savingsAccounts.unselect("Savings1");
    transactions
      .initAmountContent()
      .add("11/10/2008", "Planned: CAF", 100.00, "CAF", 1300.00, 1300.00, "Savings1")
      .add("11/10/2008", "Planned: CAF", -100.00, "CAF", -200.00, -200.00, "Main1")
      .add("11/09/2008", "Planned: CAF", 100.00, "CAF", 1200.00, 1200.00, "Savings1")
      .add("11/09/2008", "Planned: CAF", -100.00, "CAF", -100.00, -100.00, "Main1")
      .add("10/08/2008", "CAF", 100.00, "CAF", 1100.00, 1100.00, "Savings1")
      .add("10/08/2008", "CAF", -100.00, "CAF", 0.00, 0.00, "Main1")
      .check();

    views.selectBudget();
    savingsAccounts.select("Savings1");
    timeline.selectMonth("2008/08");
    savingsAccounts.checkEndOfMonthPosition("Savings1", 1100);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkEndOfMonthPosition("Savings1", 1200);
  }

  public void testImportedSavingsAccountFromExternal() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")
      .addTransaction("2008/08/10", 100.00, "CAF")
      .load();

    mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();

    budgetView.transfer.createSeries()
      .setName("CAF")
      .setToAccount("Account n. 111")
      .setFromAccount("External Account")
      .checkDateChooserIsHidden()
      .validate();
    categorization.setTransfer("CAF", "CAF");

    timeline.selectMonth("2008/08");
    savingsAccounts.select("Account n. 111");
    budgetView.transfer.alignAndPropagate("CAF");
    savingsAccounts.checkEndOfMonthPosition("Account n. 111", 1000);

    timeline.selectMonth("2008/09");
    savingsAccounts.checkEndOfMonthPosition("Account n. 111", 1100);

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: CAF", 100.00, "CAF", 1200.00, 1200.00, "Account n. 111")
      .add("11/09/2008", "Planned: CAF", 100.00, "CAF", 1100.00, 1100.00, "Account n. 111")
      .add("10/08/2008", "CAF", 100.00, "CAF", 1000.00, 1000.00, "Account n. 111")
      .check();
  }


  public void testDisableBudgetAreaIfSavingTransaction() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Prelevement")
      .load();
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")  //compte d'épargne
      .addTransaction("2008/08/10", 100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    categorization.showSelectedMonthsOnly();
    timeline.selectMonth("2008/08");

    categorization.selectTransaction("Virement");
    categorization.checkAllButTransferBudgetAreaAreDisabled();

    categorization.selectTransaction("Prelevement");
    categorization.checkAllBudgetAreasAreEnabled();

    categorization.setNewTransfer("Virement", "Virt Epargne", "Account n. 00001123", "Account n. 111");
    categorization.setNewVariable("Prelevement", "economie du mois");

    categorization.selectTransactions("Prelevement", "Virement")
      .checkAllButTransferBudgetAreaAreDisabled()
      .checkMultipleSeriesSelection()
      .setUncategorized()
      .selectTransaction("Prelevement")
      .checkTransfersPreSelected();
  }

  public void testSimpleCase() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")  //compte d'épargne
      .addTransaction("2008/08/10", 100.00, "Virement")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Prelevement")
      .addTransaction("2008/07/05", 12.00, "McDo")
      .load();

    operations.openPreferences().setFutureMonthsCount(2).validate();

    mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    views.selectBudget();
    budgetView.transfer.createSeries()
      .setName("CA")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Account n. 111")
      .validate();
    budgetView.transfer.alignAndPropagate("CA");
    categorization.showSelectedMonthsOnly();

    timeline.selectMonth("2008/08");
    categorization.setTransfer("Prelevement", "CA");
    categorization.setTransfer("Virement", "CA");
    views.selectBudget();
    budgetView.transfer.alignAndPropagate("CA");
    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: CA", -100.00, "CA", -200.00, -200.00, "Account n. 00001123")
      .add("11/10/2008", "Planned: CA", 100.00, "CA", 1200.00, 1200.00, "Account n. 111")
      .add("11/09/2008", "Planned: CA", -100.00, "CA", -100.00, -100.00, "Account n. 00001123")
      .add("11/09/2008", "Planned: CA", 100.00, "CA", 1100.00, 1100.00, "Account n. 111")
      .add("10/08/2008", "PRELEVEMENT", -100.00, "CA", 0.00, 0.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", 100.00, "CA", 1000.00, 1000.00, "Account n. 111")
      .add("05/07/2008", "MCDO", 12.00, "To categorize", 100.00, 100.00, "Account n. 00001123")
      .check();

    timeline.selectMonth("2008/10");
    savingsAccounts.checkEndOfMonthPosition("Account n. 111", 1200);
    mainAccounts.checkEndOfMonthPosition("Account n. 00001123", -200);
  }

  public void testImportedSavingsAccountWithMainAccount() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")  //compte d'épargne
      .addTransaction("2008/08/10", 100.00, "Virement")
      .addTransaction("2008/07/10", -200.00, "Prelevement")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Prelevement")
      .addTransaction("2008/07/10", 200.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();

    views.selectBudget();
    budgetView.transfer.createSeries()
      .setName("CA")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Account n. 111")
      .validate();
    budgetView.transfer.createSeries()
      .setName("Project")
      .setFromAccount("Account n. 111")
      .setToAccount("Account n. 00001123")
      .setRepeatOnceAYear()
      .toggleMonth(7)
      .validate();

    categorization.showSelectedMonthsOnly();
    timeline.selectMonth("2008/07");
    categorization.setTransfer("Prelevement", "Project");
    categorization.setTransfer("Virement", "Project");
    timeline.selectMonth("2008/08");
    categorization.setTransfer("Prelevement", "CA");
    categorization.setTransfer("Virement", "CA");

    views.selectBudget();
    budgetView.transfer
      .alignAndPropagate("CA");
    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: CA", -100.00, "CA", -200.00, -200.00, "Account n. 00001123")
      .add("11/10/2008", "Planned: CA", 100.00, "CA", 1200.00, 1200.00, "Account n. 111")
      .add("11/09/2008", "Planned: CA", -100.00, "CA", -100.00, -100.00, "Account n. 00001123")
      .add("11/09/2008", "Planned: CA", 100.00, "CA", 1100.00, 1100.00, "Account n. 111")
      .add("10/08/2008", "PRELEVEMENT", -100.00, "CA", 0.00, 0.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", 100.00, "CA", 1000.00, 1000.00, "Account n. 111")
      .add("10/07/2008", "VIREMENT", 200.00, "Project", 100.00, 100.00, "Account n. 00001123")
      .add("10/07/2008", "PRELEVEMENT", -200.00, "Project", 900.00, 900.00, "Account n. 111")
      .check();

    timeline.selectMonth("2008/10");
    savingsAccounts.checkEndOfMonthPosition("Account n. 111", 1200);
    mainAccounts.checkEndOfMonthPosition("Account n. 00001123", -200);

    timeline.selectMonth("2008/08");
    savingsAccounts.select("Account n. 111");
    budgetView.transfer.checkSeries("CA", 100, 100);
    budgetView.transfer.checkSeries("CA", 100, 100);

    mainAccounts.select("Account n. 00001123");
    budgetView.transfer.checkTotalAmounts("100.00", "100.00");
    budgetView.transfer.editSeries("CA")
      .selectAllMonths()
      .setAmount(200)
      .validate();

    savingsAccounts.select("Account n. 111");
    budgetView.transfer.editSeries("CA")
      .selectAllMonths()
      .checkAmount(200.00)
      .checkChart(new Object[][]{
        {"2008", "July", 0.00, 200.00, true},
        {"2008", "August", 100.00, 200.00, true},
        {"2008", "September", 0.00, 200.00, true},
        {"2008", "October", 0.00, 200.00, true},
      }
      )
      .validate();

    mainAccounts.select("Account n. 00001123");
    budgetView.transfer.checkTotalAmounts("100.00", "200.00");
  }

  public void testImportedSavingsAccountWithMainAccountInManual() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")
      .addTransaction("2008/08/10", 100.00, "Virement")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    views.selectBudget();
    budgetView.transfer.createSeries()
      .setName("CA")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Account n. 111")
      .selectAllMonths()
      .setAmount(50)
      .validate();

    categorization.setTransfer("Virement", "CA");
    timeline.selectAll();

    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: CA", -50.00, "CA", -100.00, -100.00, "Account n. 00001123")
      .add("11/10/2008", "Planned: CA", 50.00, "CA", 1100.00, 1100.00, "Account n. 111")
      .add("11/09/2008", "Planned: CA", -50.00, "CA", -50.00, -50.00, "Account n. 00001123")
      .add("11/09/2008", "Planned: CA", 50.00, "CA", 1050.00, 1050.00, "Account n. 111")
      .add("10/08/2008", "VIREMENT", -100.00, "CA", 0.00, 0.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", 100.00, "CA", 1000.00, 1000.00, "Account n. 111")
      .check();

    timeline.selectMonth("2008/10");
    savingsAccounts.checkEndOfMonthPosition("Account n. 111", 1100);
    mainAccounts.checkEndOfMonthPosition("Account n. 00001123", -100);

    views.selectBudget();
    timeline.selectMonth("2008/08");
    budgetView.transfer.editSeries("CA")
      .selectMonth(200808)
      .setAmount(0).validate();

    budgetView.transfer.checkTotalAmounts("100.00", "0.00");
  }

  public void testMixedTypeOfSavingsSeriesShouldUpdateCorrectlyTheBudgetView() throws Exception {
    // Un compte courant, un compte d'épargne importé, un compte d'épargne non importé

    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", 100.00, "Virement Epargne")
      .addTransaction("2008/07/06", 100.00, "Virement Epargne")
      .addTransaction("2008/08/06", 100.00, "Virement Epargne")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .addTransaction("2008/07/06", -100.00, "Virement vers Epargne")
      .addTransaction("2008/08/06", -100.00, "Virement vers Epargne")
      .load();

    operations.openPreferences().setFutureMonthsCount(2).validate();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();
    accounts.createSavingsAccount("Virt Epargne", 1000.00);

    views.selectBudget();
    budgetView.transfer.createSeries()
      .setName("Virement CAF")
      .setToAccount("Virt Epargne")
      .setFromAccount("External account")
      .selectAllMonths()
      .setAmount("300")
      .setDay("5")
      .validate();

    budgetView.transfer.createSeries()
      .setName("Placement")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Account n. 111222")
      .validate();

    categorization.setTransfer("Virement Epargne", "Placement");
    categorization.setTransfer("Virement vers Epargne", "Placement");

    timeline.selectMonth("2008/06");
    budgetView.transfer.alignAndPropagate("Placement");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: Virement CAF", 300.00, "Virement CAF", 1900.00, 5100.00, "Virt Epargne")
      .add("04/10/2008", "Planned: Placement", -100.00, "Placement", -200.00, -200.00, "Account n. 00001123")
      .add("04/10/2008", "Planned: Placement", 100.00, "Placement", 3200.00, 4800.00, "Account n. 111222")
      .add("11/09/2008", "Planned: Virement CAF", 300.00, "Virement CAF", 1600.00, 4700.00, "Virt Epargne")
      .add("04/09/2008", "Planned: Placement", -100.00, "Placement", -100.00, -100.00, "Account n. 00001123")
      .add("04/09/2008", "Planned: Placement", 100.00, "Placement", 3100.00, 4400.00, "Account n. 111222")
      .add("11/08/2008", "Planned: Virement CAF", 300.00, "Virement CAF", 1300.00, 4300.00, "Virt Epargne")
      .add("06/08/2008", "VIREMENT VERS EPARGNE", -100.00, "Placement", 0.00, 0.00, "Account n. 00001123")
      .add("06/08/2008", "VIREMENT EPARGNE", 100.00, "Placement", 3000.00, 4000.00, "Account n. 111222")
      .add("06/07/2008", "VIREMENT VERS EPARGNE", -100.00, "Placement", 100.00, 100.00, "Account n. 00001123")
      .add("06/07/2008", "VIREMENT EPARGNE", 100.00, "Placement", 2900.00, 3900.00, "Account n. 111222")
      .add("06/06/2008", "VIREMENT VERS EPARGNE", -100.00, "Placement", 200.00, 200.00, "Account n. 00001123")
      .add("06/06/2008", "VIREMENT EPARGNE", 100.00, "Placement", 2800.00, 3800.00, "Account n. 111222")
      .check();

    timeline.selectMonth("2008/06");
    savingsAccounts.checkEndOfMonthPosition("Virt Epargne", 1000);
    savingsAccounts.checkEndOfMonthPosition("Account n. 111222", 2800);
    savingsAccounts.checkReferencePosition(4000, "2008/08/06");

    timeline.selectMonth("2008/08");
    savingsAccounts.checkEndOfMonthPosition("Virt Epargne", 1300);
    savingsAccounts.checkEndOfMonthPosition("Account n. 111222", 3000);
    savingsAccounts.checkReferencePosition(4000, "2008/08/06");

    savingsAccounts.select("Virt Epargne");
    budgetView.transfer.checkSeries("Virement CAF", 0, 300);

    savingsAccounts.select("Account n. 111222");
    budgetView.transfer.checkSeries("Placement", 100, 100);

    timeline.selectMonth("2008/09");
    savingsAccounts.checkEndOfMonthPosition("Virt Epargne", 1600);
    savingsAccounts.checkEndOfMonthPosition("Account n. 111222", 3100);

    savingsAccounts.select("Virt Epargne");
    budgetView.transfer.checkSeries("Virement CAF", 0, 300);
    savingsAccounts.select("Account n. 111222");
    budgetView.transfer.checkSeries("Placement", 0, 100);

    mainAccounts.select("Account n. 00001123");
    timeline.selectMonth("2008/06");
    budgetView.transfer.checkTotalGauge(-100, -100);
    timeline.selectMonth("2008/08");
    budgetView.transfer.checkTotalGauge(-100, -100);
    timeline.selectMonth("2008/09");
    budgetView.transfer.checkTotalGauge(0, -100);

    timeline.selectMonth("2008/06");
    budgetView.transfer.checkSeries("Placement", "100.00", "100.00");
    budgetView.transfer.checkSeriesNotPresent("Virement CAF");

    savingsAccounts.select("Account n. 111222");
    budgetView.transfer.checkSeries("Placement", "+100.00", "+100.00");

    timeline.selectMonth("2008/09");
    mainAccounts.select("Account n. 00001123");
    budgetView.transfer.checkSeries("Placement", "0.00", "100.00");
    budgetView.transfer.checkSeriesNotPresent("Virement CAF");

    savingsAccounts.select("Account n. 111222");
    budgetView.transfer.checkSeries("Placement", "0.00", "+100.00");

    savingsAccounts.unselect("Account n. 111222");
    budgetView.transfer.editSeries("Placement").deleteSavingsSeriesWithConfirmation();
    budgetView.transfer.checkSeriesNotPresent("Virement CAF");

    savingsAccounts.select("Virt Epargne");
    budgetView.transfer.editSeries("Virement CAF").deleteCurrentSeriesWithoutConfirmation();

    String fileName = operations.backup(this);
    operations.restore(fileName);

    savingsAccounts.select("Virt Epargne");
    budgetView.transfer.checkNoSeriesShown();
  }

  public void testSavingsGauge() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")  //compte d'épargne
      .addTransaction("2008/08/12", -100.00, "P3 CE")
      .addTransaction("2008/08/11", -100.00, "P2 CE")
      .addTransaction("2008/08/10", -100.00, "P1 CE")
      .addTransaction("2008/08/12", 20.00, "V3 CE")
      .addTransaction("2008/08/11", 50.00, "V2 CE")
      .addTransaction("2008/08/10", 50.00, "V1 CE")
      .addTransaction("2008/07/10", 100.00, "Virement CE")
      .addTransaction("2008/07/10", -200.00, "Prelevement CE")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/12", 100.00, "V3 CC")
      .addTransaction("2008/08/11", 100.00, "V2 CC")
      .addTransaction("2008/08/10", 100.00, "V1 CC")
      .addTransaction("2008/08/12", -20.00, "P3 CC")
      .addTransaction("2008/08/11", -50.00, "P2 CC")
      .addTransaction("2008/08/10", -50.00, "P1 CC")
      .addTransaction("2008/07/10", -100.00, "Prelevement CC")
      .addTransaction("2008/07/10", 200.00, "Virement CC")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    views.selectBudget();
    budgetView.transfer.createSeries()
      .setName("CA")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Account n. 111")
      .validate();
    budgetView.transfer.createSeries()
      .setName("Project")
      .setFromAccount("Account n. 111")
      .setToAccount("Account n. 00001123")
      .setRepeatCustom()
      .setStartDate(200807)
      .setEndDate(200809)
      .validate();

    categorization.showSelectedMonthsOnly();

    timeline.selectMonth("2008/07");
    categorization.setTransfer("Prelevement CE", "Project");
    categorization.setTransfer("Virement CE", "CA");
    categorization.setTransfer("Virement CC", "Project");
    categorization.setTransfer("Prelevement CC", "CA");
    views.selectBudget();
    budgetView.transfer.alignAndPropagate("CA");

    timeline.selectMonth("2008/08");
    categorization.setTransfer("P1 CC", "CA");
    categorization.setTransfer("P2 CC", "CA");
    categorization.setTransfer("P3 CC", "CA");
    categorization.setTransfer("P1 CE", "Project");
    categorization.setTransfer("P2 CE", "Project");
    categorization.setTransfer("P3 CE", "Project");
    categorization.setTransfer("V1 CC", "Project");
    categorization.setTransfer("V2 CC", "Project");
    categorization.setTransfer("V3 CC", "Project");
    categorization.setTransfer("V1 CE", "CA");
    categorization.setTransfer("V2 CE", "CA");
    categorization.setTransfer("V3 CE", "CA");

    budgetView.transfer.checkSeries("CA", "120.00", "100.00");
    transactions.initAmountContent()
      .add("12/08/2008", "P3 CC", -20.00, "CA", 0.00, 0.00, "Account n. 00001123")
      .add("12/08/2008", "V3 CC", 100.00, "Project", 20.00, 20.00, "Account n. 00001123")
      .add("12/08/2008", "V3 CE", 20.00, "CA", 1000.00, 1000.00, "Account n. 111")
      .add("12/08/2008", "P3 CE", -100.00, "Project", 980.00, 980.00, "Account n. 111")
      .add("11/08/2008", "P2 CC", -50.00, "CA", -80.00, -80.00, "Account n. 00001123")
      .add("11/08/2008", "V2 CC", 100.00, "Project", -30.00, -30.00, "Account n. 00001123")
      .add("11/08/2008", "V2 CE", 50.00, "CA", 1080.00, 1080.00, "Account n. 111")
      .add("11/08/2008", "P2 CE", -100.00, "Project", 1030.00, 1030.00, "Account n. 111")
      .add("10/08/2008", "P1 CC", -50.00, "CA", -130.00, -130.00, "Account n. 00001123")
      .add("10/08/2008", "V1 CC", 100.00, "Project", -80.00, -80.00, "Account n. 00001123")
      .add("10/08/2008", "V1 CE", 50.00, "CA", 1130.00, 1130.00, "Account n. 111")
      .add("10/08/2008", "P1 CE", -100.00, "Project", 1080.00, 1080.00, "Account n. 111")
      .check();

    String fileName = operations.backup(this);

    budgetView.transfer.editSeries("Project")
      .deleteSavingsSeriesWithConfirmation();
    budgetView.transfer.editSeries("CA")
      .deleteSavingsSeriesWithConfirmation();

    checkDeleteSeries();

    operations.restore(fileName);

    savingsAccounts.select("Account n. 111");
    budgetView.transfer.editSeries("CA").deleteSavingsSeriesWithConfirmation();
    budgetView.transfer.editSeries("Project").deleteSavingsSeriesWithConfirmation();

    checkDeleteSeries();
  }

  private void checkDeleteSeries() throws Exception {

    String fileName = operations.backup(this);
    operations.restore(fileName);

    timeline.selectAll();
    transactions.initContent()
      .add("12/08/2008", TransactionType.PRELEVEMENT, "P3 CC", "", -20.00)
      .add("12/08/2008", TransactionType.VIREMENT, "V3 CC", "", 100.00)
      .add("12/08/2008", TransactionType.VIREMENT, "V3 CE", "", 20.00)
      .add("12/08/2008", TransactionType.PRELEVEMENT, "P3 CE", "", -100.00)
      .add("11/08/2008", TransactionType.PRELEVEMENT, "P2 CC", "", -50.00)
      .add("11/08/2008", TransactionType.VIREMENT, "V2 CC", "", 100.00)
      .add("11/08/2008", TransactionType.VIREMENT, "V2 CE", "", 50.00)
      .add("11/08/2008", TransactionType.PRELEVEMENT, "P2 CE", "", -100.00)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "P1 CC", "", -50.00)
      .add("10/08/2008", TransactionType.VIREMENT, "V1 CC", "", 100.00)
      .add("10/08/2008", TransactionType.VIREMENT, "V1 CE", "", 50.00)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "P1 CE", "", -100.00)
      .add("10/07/2008", TransactionType.VIREMENT, "VIREMENT CC", "", 200.00)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "PRELEVEMENT CC", "", -100.00)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "PRELEVEMENT CE", "", -200.00)
      .add("10/07/2008", TransactionType.VIREMENT, "VIREMENT CE", "", 100.00)
      .check();
  }

  public void testChangeAccountDirectionDoesNotChangeBudgetSign() throws Exception {
    accounts.createMainAccount("Main", 99);
    accounts.createSavingsAccount("Virt Epargne", 1000.);

    views.selectBudget();
    SeriesEditionDialogChecker editionDialogChecker = budgetView.transfer.createSeries();
    editionDialogChecker
      .setName("Test")
      .setFromAccount("Main")
      .setToAccount("External")
      .selectAllMonths()
      .setAmount("300")
      .checkChart(new Object[][]{
        {"2008", "August", 0.00, 300.00, true}
      });
    editionDialogChecker
      .setFromAccount("External")
      .setToAccount("Main")
      .checkChart(new Object[][]{
        {"2008", "August", 0.00, 300.00, true}
      })
      .validate();
  }

  public void testBothNotImportedAccount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    accounts.createSavingsAccount("Savings 1", 1000.00);
    accounts.createSavingsAccount("Savings 2", 1000.00);

    budgetView.transfer.createSeries()
      .setName("Test")
      .setFromAccount("Savings 1")
      .setToAccount("Savings 2")
      .selectAllMonths()
      .setAmount("300")
      .validate();

    timeline.selectMonth("2008/06");
    savingsAccounts.select("Savings 1");
    budgetView.transfer.checkSeries("Test", 0, -300.00);

    savingsAccounts.select("Savings 2");
    budgetView.transfer.checkSeries("Test", 0, 300.00);

    savingsAccounts.select("Savings 1");
    budgetView.transfer.editSeries("Test").deleteCurrentSeriesWithoutConfirmation();

    String fileName = operations.backup(this);
    operations.restore(fileName);

    timeline.selectMonth("2008/06");
    savingsAccounts.select("Savings 1");
    budgetView.transfer.checkNoSeriesShown();

    savingsAccounts.select("Savings 2");
    budgetView.transfer.checkNoSeriesShown();
  }

  public void testSavingsAccountWithNoTransactionShouldNotBeIgnored() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    categorization.setNewTransfer("Virement", "Virt Epargne", "Account n. 00001123", "External account");

    accounts.createNewAccount()
      .setAsSavings()
      .setName("Livret")
      .selectBank("ING Direct")
      .setPosition(0)
      .validate();
    accounts.createNewAccount()
      .setAsSavings()
      .setName("Livret 2")
      .selectBank("ING Direct")
      .validate();
    categorization.selectTransactions("Virement")
      .editSeries("Virt Epargne")
      .setToAccount("Livret")
      .validate();
    categorization.selectTransfers().selectSeries("Virt Epargne");
    views.selectBudget();
    budgetView.transfer.alignAndPropagate("Virt Epargne");

    timeline.selectMonth("2008/07");
    savingsAccounts.checkEndOfMonthPosition("Livret", 200);
    savingsAccounts.checkEndOfMonthPosition("Livret 2", 0);

    timeline.selectMonth("2008/08");
    savingsAccounts.checkEndOfMonthPosition("Livret", 300);
    savingsAccounts.checkEndOfMonthPosition("Livret 2", 0);
  }

  public void testInverseAccountAfterCategorizationIsNotPossible() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .load();

    accounts.createNewAccount()
      .setAsSavings()
      .setName("Livret")
      .selectBank("ING Direct")
      .setPosition(100)
      .validate();
    categorization.setNewTransfer("Virement", "Virt Epargne", "Account n. 00001123", "Livret");

    budgetView.transfer.editSeries("Virt Epargne")
      .checkFromContentEquals("Account n. 00001123")
      .checkToContentEquals("Livret", "External account")
      .validate();

    savingsAccounts.select("Livret");
    budgetView.transfer.editSeries("Virt Epargne")
      .checkFromContentEquals("Account n. 00001123")
      .checkToContentEquals("Livret", "External account")
      .validate();
  }

  public void testSavingsAccounts() throws Exception {

    accounts.createNewAccount()
      .setName("Main")
      .setAccountNumber("4321")
      .selectBank("CIC")
      .setAsMain()
      .checkIsMain()
      .setPosition(99.0)
      .validate();
    accounts.createSavingsAccount("Virt Epargne", 1000.00);

    savingsAccounts.select("Virt Epargne");
    budgetView.transfer.createSeries()
      .setName("Virement")
      .setToAccount("Virt Epargne")
      .setFromAccount("Main")
      .selectAllMonths()
      .setAmount("300")
      .setDay("5")
      .validate();

    savingsAccounts.checkAccount("Virt Epargne", 1000.00, "2008/08/01");
  }

  public void testAllInManualTransactionModeFromSavingsToMainWithCreationOfTransactionInMain() throws Exception {
    accounts.createNewAccount().setName("Main")
      .setAsMain()
      .setPosition(1000)
      .selectBank(SOCIETE_GENERALE)
      .validate();

    accounts.createNewAccount()
      .setName("Savings")
      .selectBank(SOCIETE_GENERALE)
      .setAsSavings()
      .setPosition(1000)
      .validate();

    transactionCreation.show()
      .selectAccount("Savings")
      .setAmount(-100)
      .setLabel("Financement")
      .setDay(2)
      .create();

    categorization.selectTransactions("Financement")
      .selectTransfers().createSeries()
      .setName("Savings Series")
      .setFromAccount("Savings")
      .setToAccount("Main")
      .validate();
    categorization.setTransfer("Financement", "Savings Series");

    savingsAccounts.select("Savings");
    transactions
      .initAmountContent()
      .add("02/08/2008", "FINANCEMENT", -100.00, "Savings Series", 900.00, 900.00, "Savings")
      .check();
  }

  public void testTakePositionDateIntoAccountIfNoOperationsInThePast() throws Exception {

    operations.openPreferences().setFutureMonthsCount(6).validate();

    accounts.createNewAccount().setName("Main")
      .setAsMain()
      .setPosition(1000)
      .selectBank(SOCIETE_GENERALE)
      .validate();

    accounts.createNewAccount().setName("Savings")
      .selectBank(SOCIETE_GENERALE)
      .setAsSavings()
      .setPosition(1000)
      .validate();

    mainAccounts
      .checkReferencePosition(1000, "2008/08/01");

    savingsAccounts
      .checkReferencePosition(1000, "2008/08/01");

    views.selectBudget();
    budgetView.recurring.createSeries()
      .setName("EDF")
      .setTargetAccount("Main")
      .selectAllMonths()
      .setAmount("50")
      .validate();

    timeline.selectMonth("2008/09");

    views.selectBudget();
    mainAccounts.checkEndOfMonthPosition("Main", 900.00);
  }

  public void testCheckComboAccountContents() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", 100.00, "Virement Epargne")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .load();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();

    categorization.selectAllTransactions()
      .selectTransfers()
      .createSeries()
      .setName("Virt Epargne")
      .checkToContentEquals("Account n. 111222")
      .checkToAccount("Account n. 111222")
      .checkFromContentEquals("Account n. 00001123")
      .checkFromAccount("Account n. 00001123")
      .validate();

    categorization
      .selectUncategorized()
      .selectTransaction("Virement vers Epargne")
      .checkSavingsSeriesIsSelected("Virt Epargne")
      .selectTransaction("Virement Epargne")
      .checkSavingsSeriesIsSelected("Virt Epargne");
  }

  public void testCreatingSavingsFromCategorisationDoNotAssign() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", -100.00, "Virement Epargne")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .load();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();

    mainAccounts.edit("Account n. 00001123")
      .setAsSavings()
      .validate();

    categorization.selectAllTransactions()
      .selectTransfers()
      .createSeries()
      .setName("Virt Epargne")
      .checkToContentEquals("Account n. 111222", "Account n. 00001123", "External account")
      .setToAccount("Account n. 111222")
      .checkFromContentEquals("Account n. 111222", "Account n. 00001123", "External account")
      .setFromAccount("External account")
      .validate();

    categorization.selectTransaction("Virement vers Epargne")
      .checkTransfersPreSelected()
      .selectTransfers()
      .checkContainsNoSeries();

    categorization.selectTransaction("Virement Epargne")
      .checkTransfersPreSelected()
      .selectTransfers()
      .checkContainsNoSeries();
  }

  public void testDeleteSavingSeriesAskForConfirmation() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .load();

    mainAccounts.edit("Account n. 00001123")
      .setAsSavings()
      .validate();

    categorization.selectAllTransactions()
      .selectTransfers()
      .createSeries()
      .setName("Virt Epargne")
      .setToAccount("External account")
      .setFromAccount("Account n. 00001123")
      .validate();

    categorization.selectTransaction("Virement vers Epargne")
      .checkSavingsSeriesIsSelected("Virt Epargne");

    SeriesEditionDialogChecker seriesEditionDialogChecker = categorization.editSeries("Virt Epargne");
    seriesEditionDialogChecker
      .deleteSavingsSeriesWithConfirmationAndCancel()
      .cancel();
    seriesEditionDialogChecker.cancel();
  }

  public void testCategorisationOnSavingCreation() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", -100.00, "Virement Epargne")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .load();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();

    mainAccounts.edit("Account n. 00001123")
      .setAsSavings()
      .validate();

    categorization.selectTransaction("Virement Epargne")
      .selectTransfers()
      .createSeries()
      .setName("Virt Epargne")
      .checkFromContentEquals("Account n. 111222")
      .checkToContentEquals("Account n. 00001123", "External account")
      .setToAccount("Account n. 00001123")
      .validate();

    categorization.selectTransaction("Virement Epargne")
      .checkSavingsSeriesIsSelected("Virt Epargne");
  }

  public void testCanNotChooseTheSameAccount() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", -100.00, "Virement Epargne")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .load();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();

    mainAccounts.edit("Account n. 00001123")
      .setAsSavings()
      .validate();

    SeriesEditionDialogChecker editionDialogChecker = categorization.selectAllTransactions()
      .selectTransfers()
      .createSeries();
    editionDialogChecker
      .setName("Virt Epargne")
      .setToAccount("Account n. 00001123")
      .setFromAccount("Account n. 00001123")
      .checkSavingsMessageVisibility(true)
      .checkOkEnabled(false);
    editionDialogChecker
      .setFromAccount("Account n. 111222")
      .checkSavingsMessageVisibility(false)
      .checkOkEnabled(true);
    editionDialogChecker
      .setToAccount("Account n. 111222")
      .checkSavingsMessageVisibility(true)
      .checkOkEnabled(false)
      .cancel();
  }

  public void testDeleteSeriesForBothImportedAccountWithTransactionInFrom() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", -100.00, "Virement Epargne")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .load();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();

    mainAccounts.edit("Account n. 00001123")
      .setAsSavings()
      .validate();

    categorization.selectTransaction("Virement vers Epargne")
      .selectTransfers()
      .createSeries()
      .setName("Virt Epargne")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Account n. 111222")
      .validate();

    categorization
      .selectTransaction("Virement vers Epargne")
      .editSeries("Virt Epargne")
      .deleteSavingsSeriesWithConfirmation();

    categorization.checkBudgetAreaSelectionPanelDisplayed();
  }

  public void testCheckComboAccountContentsWithNotImportedAccount() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", 100.00, "Virement de l'Epargne")
      .load();

    SeriesEditionDialogChecker editionDialogChecker = categorization.selectAllTransactions()
      .selectTransfers()
      .createSeries()
      .setName("Financement");
    editionDialogChecker.createAccount().setName("CODEVI")
      .selectBank("CIC")
      .setPosition(0).validate();
    editionDialogChecker
      .checkToContentEquals("Account n. 00001123")
      .checkToAccount("Account n. 00001123")
      .setFromAccount("CODEVI")
      .checkFromContentEquals("CODEVI", "External account")
      .validate();

    SeriesEditionDialogChecker seriesEdition = categorization.selectTransaction("Virement de l'Epargne")
      .editSeries("Financement");
    seriesEdition
      .createAccount()
      .setName("Livret A")
      .selectBank("CIC")
      .setPosition(0)
      .validate();
    seriesEdition.checkFromContentEquals("Livret A", "CODEVI", "External account")
      .setFromAccount("Livret A")
      .validate();

    categorization.selectAllTransactions()
      .selectTransfers()
      .selectSeries("Financement");

    views.selectBudget();
    budgetView.transfer.alignAndPropagate("Financement");
    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("04/10/2008", TransactionType.PLANNED, "Planned: Financement", "", -100.00, "Financement")
      .add("04/10/2008", TransactionType.PLANNED, "Planned: Financement", "", 100.00, "Financement")
      .add("04/09/2008", TransactionType.PLANNED, "Planned: Financement", "", -100.00, "Financement")
      .add("04/09/2008", TransactionType.PLANNED, "Planned: Financement", "", 100.00, "Financement")
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Financement", "", -100.00, "Financement")
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Financement", "", 100.00, "Financement")
      .add("04/07/2008", TransactionType.PLANNED, "Planned: Financement", "", -100.00, "Financement")
      .add("04/07/2008", TransactionType.PLANNED, "Planned: Financement", "", 100.00, "Financement")
      .add("06/06/2008", TransactionType.PRELEVEMENT, "Planned: Financement", "", -100.00, "Financement")
      .add("06/06/2008", TransactionType.VIREMENT, "VIREMENT DE L'EPARGNE", "", 100.00, "Financement")
      .check();
  }

  public void testDeleteSavingsAccount() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", 100.00, "Virement Epargne")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .load();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();

    categorization.selectTransaction("Virement vers Epargne")
      .selectTransfers()
      .createSeries()
      .setName("Virt Epargne")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Account n. 111222")
      .validate();

    categorization.selectTransaction("Virement Epargne")
      .selectTransfers()
      .selectSeries("Virt Epargne");

    transactions.initContent()
      .add("06/06/2008", TransactionType.PRELEVEMENT, "VIREMENT VERS EPARGNE", "", -100.00, "Virt Epargne")
      .add("06/06/2008", TransactionType.VIREMENT, "VIREMENT EPARGNE", "", 100.00, "Virt Epargne")
      .check();

    savingsAccounts.edit("Account n. 111222").openDelete().validate();
    transactions.initContent()
      .add("06/06/2008", TransactionType.PRELEVEMENT, "VIREMENT VERS EPARGNE", "", -100.00)
      .check();
  }

  public void testChangePlannedFromBudgetView() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/08/06", -100.00, "Virement vers Epargne")
      .load();

    accounts.createSavingsAccount("ING", 1000.);
    views.selectBudget();
    mainAccounts.checkEndOfMonthPosition("Account n. 00001123", 0);
    budgetView.transfer.createSeries().setName("Main to Savings")
      .setFromAccount("Account n. 00001123")
      .setToAccount("External account")
      .validate();
    budgetView.transfer.editPlannedAmount("Main to Savings").setPropagationEnabled().setAmountAndValidate("500");
    budgetView.transfer.editSeries("Main to Savings")
      .selectMonth(200808)
      .checkAmount("500.00")
      .cancel();
    mainAccounts.checkEndOfMonthPosition("Account n. 00001123", -500.);
    timeline.selectMonth("2008/09");
    mainAccounts.checkEndOfMonthPosition("Account n. 00001123", -1000.);
    timeline.selectMonth("2008/10");
    mainAccounts.checkEndOfMonthPosition("Account n. 00001123", -1500.);

    timeline.selectMonth("2008/08");
    budgetView.transfer.createSeries().setName("Savings to Main")
      .setToAccount("Account n. 00001123")
      .setFromAccount("External account")
      .validate();
    budgetView.transfer.editPlannedAmount("Savings to Main").setPropagationEnabled().setAmountAndValidate("500");
    budgetView.transfer.editSeries("Savings to Main")
      .selectMonth(200808)
      .checkAmount("500.00")
      .cancel();

    mainAccounts.checkEndOfMonthPosition("Account n. 00001123", 0.);

    timeline.selectMonth("2008/09");
    mainAccounts.checkEndOfMonthPosition("Account n. 00001123", 0.);
    timeline.selectMonth("2008/10");
    mainAccounts.checkEndOfMonthPosition("Account n. 00001123", 0.);
  }

  public void testChange() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", 100.00, "Virement Epargne")
      .addTransaction("2008/07/06", 100.00, "Virement Epargne")
      .addTransaction("2008/08/06", 100.00, "Virement Epargne")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .addTransaction("2008/07/06", -100.00, "Virement vers Epargne")
      .addTransaction("2008/08/06", -100.00, "Virement vers Epargne")
      .load();

    operations.openPreferences().setFutureMonthsCount(2).validate();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();
    views.selectBudget();

    budgetView.transfer.createSeries()
      .setName("Placement")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Account n. 111222")
      .selectAllMonths()
      .setAmount("100")
      .validate();

    categorization
      .setTransfer("Virement vers Epargne", "Placement");

    categorization
      .setTransfer("Virement Epargne", "Placement");

    views.selectBudget();
    budgetView.transfer.editPlannedAmount("Placement")
      .setAmount("200")
      .validate();
    budgetView.transfer.editSeries("Placement")
      .checkAmount("200.00")
      .cancel();
  }

  public void testAutomaticalyCreateSeriesAtSavingCreationWithOtherSavingAccount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .addTransaction("2008/06/06", 100.00, "Virement de Epargne")
      .load();

    categorization.selectTransaction("Virement vers Epargne")
      .selectTransfers()
      .createSavingsAccount().setName("suisse account")
      .selectBank("CIC").validate();

    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", 100.00, "Virement de courant")
      .addTransaction("2008/06/06", -100.00, "Virement vers courant")
      .loadInNewAccount();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();

    views.selectBudget();
    budgetView.transfer.createSeries().setName("To account n. 111222").setFromAccount("Account n. 00001123").setToAccount("Account n. 111222").validate();
    budgetView.transfer.createSeries().setName("From account n. 111222").setFromAccount("Account n. 111222").setToAccount("Account n. 00001123").validate();
    categorization.selectTransaction("Virement de Epargne")
      .selectTransfers()
      .selectSeries("From account n. 111222")
      .editSeries("From account n. 111222").alignPlannedAndActual().setPropagationEnabled().validate();
    categorization.selectTransaction("Virement vers Epargne")
      .selectTransfers()
      .selectSeries("To account n. 111222")
      .editSeries("To account n. 111222").alignPlannedAndActual().setPropagationEnabled().validate();
    categorization.selectTransaction("Virement de courant")
      .selectTransfers()
      .checkDoesNotContainSeries("suisse account")
      .selectSeries("To account n. 111222")
      .editSeries("To account n. 111222").alignPlannedAndActual().setPropagationEnabled().validate();
    categorization.selectTransaction("Virement vers courant")
      .selectTransfers()
      .selectSeries("From account n. 111222")
      .editSeries("From account n. 111222").alignPlannedAndActual().setPropagationEnabled().validate();

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("04/08/2008", "Planned: From account n. 111222", -100.00, "From account n. 111222", 3000.00, 3000.00, "Account n. 111222")
      .add("04/08/2008", "Planned: To account n. 111222", 100.00, "To account n. 111222", 3100.00, 3100.00, "Account n. 111222")
      .add("04/08/2008", "Planned: To account n. 111222", -100.00, "To account n. 111222", 0.0,0.0, "Account n. 00001123")
      .add("04/08/2008", "Planned: From account n. 111222", 100.00, "From account n. 111222", 100., 100., "Account n. 00001123")
      .add("04/07/2008", "Planned: From account n. 111222", -100.00, "From account n. 111222", 3000.00, 3000.00, "Account n. 111222")
      .add("04/07/2008", "Planned: To account n. 111222", 100.00, "To account n. 111222", 3100.00, 3100.00, "Account n. 111222")
      .add("04/07/2008", "Planned: To account n. 111222", -100.00, "To account n. 111222", 0.00, 0.00, "Account n. 00001123")
      .add("04/07/2008", "Planned: From account n. 111222", 100.00, "From account n. 111222", 100.00, 100.00, "Account n. 00001123")
      .add("06/06/2008", "VIREMENT VERS COURANT", -100.00, "From account n. 111222", 3000.00, 3000.00, "Account n. 111222")
      .add("06/06/2008", "VIREMENT DE COURANT", 100.00, "To account n. 111222", 3100.00, 3100.00, "Account n. 111222")
      .add("06/06/2008", "VIREMENT DE EPARGNE", 100.00, "From account n. 111222", 0.00, 0.00, "Account n. 00001123")
      .add("06/06/2008", "VIREMENT VERS EPARGNE", -100.00, "To account n. 111222", -100.00, -100.00, "Account n. 00001123")
      .check();

    views.selectBudget();
    budgetView.transfer.editPlannedAmount("To account n. 111222")
      .setAmount(150)
      .validate();
    timeline.selectMonth("2008/06");
    budgetView.transfer.checkSeries("To account n. 111222", "100.00", "150.00");
  }

  public void testImportOnMirrorAccount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .addTransaction("2008/06/06", 100.00, "Virement de Epargne")
      .load();

    accounts.createSavingsAccount("Virt Epargne", 100.);
    budgetView.transfer.createSavingSeries("To account epargne", "Account n. 00001123", "Virt Epargne");
    budgetView.transfer.createSavingSeries("From account epargne", "Virt Epargne", "Account n. 00001123");

    categorization.selectTransaction("Virement de Epargne")
      .selectTransfers()
      .selectSeries("From account epargne")
      .editSeries("From account epargne").alignPlannedAndActual().setPropagationEnabled().validate();
    categorization.selectTransaction("Virement vers Epargne")
      .selectTransfers()
      .selectSeries("To Account epargne")
      .editSeries("To account epargne").alignPlannedAndActual().setPropagationEnabled().validate();

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("04/08/2008", "Planned: From account epargne", -100.00, "From account epargne", 100.00, 100.00, "Virt Epargne")
      .add("04/08/2008", "Planned: To account epargne", 100.00, "To account epargne", 200.00, 200.00, "Virt Epargne")
      .add("04/08/2008", "Planned: To account epargne", -100.00, "To account epargne", 0.00, 0.00, "Account n. 00001123")
      .add("04/08/2008", "Planned: From account epargne", 100.00, "From account epargne", 100.00, 100.00, "Account n. 00001123")
      .add("04/07/2008", "Planned: From account epargne", -100.00, "From account epargne", 100.00, 100.00, "Virt Epargne")
      .add("04/07/2008", "Planned: To account epargne", 100.00, "To account epargne", 200.00, 200.00, "Virt Epargne")
      .add("04/07/2008", "Planned: To account epargne", -100.00, "To account epargne", 0.00, 0.00, "Account n. 00001123")
      .add("04/07/2008", "Planned: From account epargne", 100.00, "From account epargne", 100.00, 100.00, "Account n. 00001123")
      .add("06/06/2008", "Planned: From account epargne", -100.00, "From account epargne", 100.00, 100.00, "Virt Epargne")
      .add("06/06/2008", "Planned: To account epargne", 100.00, "To account epargne", 200.00, 200.00, "Virt Epargne")
      .add("06/06/2008", "VIREMENT DE EPARGNE", 100.00, "From account epargne", 0.00, 0.00, "Account n. 00001123")
      .add("06/06/2008", "VIREMENT VERS EPARGNE", -100.00, "To account epargne", -100.00, -100.00, "Account n. 00001123")
      .check();

    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", 100.00, "Virement de courant")
      .addTransaction("2008/06/06", -100.00, "Virement vers courant")
      .load("Account n. 111", "Virt Epargne");

    savingsAccounts.changePosition("Virt Epargne", 3000., "");

    timeline.selectAll();
    transactions.initAmountContent()
      .add("04/08/2008", "Planned: From account epargne", -100.00, "From account epargne", 3000.00, 3000.00, "Virt Epargne")
      .add("04/08/2008", "Planned: To account epargne", 100.00, "To account epargne", 3100.00, 3100.00, "Virt Epargne")
      .add("04/08/2008", "Planned: To account epargne", -100.00, "To account epargne", 0.00, 0.00, "Account n. 00001123")
      .add("04/08/2008", "Planned: From account epargne", 100.00, "From account epargne", 100.00, 100.00, "Account n. 00001123")
      .add("04/07/2008", "Planned: From account epargne", -100.00, "From account epargne", 3000.00, 3000.00, "Virt Epargne")
      .add("04/07/2008", "Planned: To account epargne", 100.00, "To account epargne", 3100.00, 3100.00, "Virt Epargne")
      .add("04/07/2008", "Planned: To account epargne", -100.00, "To account epargne", 0.00, 0.00, "Account n. 00001123")
      .add("04/07/2008", "Planned: From account epargne", 100.00, "From account epargne", 100.00, 100.00, "Account n. 00001123")
      .add("06/06/2008", "Planned: From account epargne", -100.00, "From account epargne", 3000.00, 3000.00, "Virt Epargne")
      .add("06/06/2008", "Planned: To account epargne", 100.00, "To account epargne", 3100.00, 3100.00, "Virt Epargne")
      .add("06/06/2008", "VIREMENT VERS COURANT", -100.00, "To categorize", 3000.00, 3000.00, "Virt Epargne")
      .add("06/06/2008", "VIREMENT DE COURANT", 100.00, "To categorize", 3100.00, 3100.00, "Virt Epargne")
      .add("06/06/2008", "VIREMENT DE EPARGNE", 100.00, "From account epargne", 0.00, 0.00, "Account n. 00001123")
      .add("06/06/2008", "VIREMENT VERS EPARGNE", -100.00, "To account epargne", -100.00, -100.00, "Account n. 00001123")
      .check();

    categorization.selectTransaction("Virement de courant")
      .selectTransfers()
      .selectSeries("To account epargne");
    categorization.selectTransaction("Virement vers courant")
      .selectTransfers()
      .selectSeries("From account epargne");

    timeline.selectAll();
    transactions.initAmountContent()
      .add("04/08/2008", "Planned: From account epargne", -100.00, "From account epargne", 3000.00, 3000.00, "Virt Epargne")
      .add("04/08/2008", "Planned: To account epargne", 100.00, "To account epargne", 3100.00, 3100.00, "Virt Epargne")
      .add("04/08/2008", "Planned: To account epargne", -100.00, "To account epargne", 0.00, 0.00, "Account n. 00001123")
      .add("04/08/2008", "Planned: From account epargne", 100.00, "From account epargne", 100.00, 100.00, "Account n. 00001123")
      .add("04/07/2008", "Planned: From account epargne", -100.00, "From account epargne", 3000.00, 3000.00, "Virt Epargne")
      .add("04/07/2008", "Planned: To account epargne", 100.00, "To account epargne", 3100.00, 3100.00, "Virt Epargne")
      .add("04/07/2008", "Planned: To account epargne", -100.00, "To account epargne", 0.00, 0.00, "Account n. 00001123")
      .add("04/07/2008", "Planned: From account epargne", 100.00, "From account epargne", 100.00, 100.00, "Account n. 00001123")
      .add("06/06/2008", "VIREMENT VERS COURANT", -100.00, "From account epargne", 3000.00, 3000.00, "Virt Epargne")
      .add("06/06/2008", "VIREMENT DE COURANT", 100.00, "To account epargne", 3100.00, 3100.00, "Virt Epargne")
      .add("06/06/2008", "VIREMENT DE EPARGNE", 100.00, "From account epargne", 0.00, 0.00, "Account n. 00001123")
      .add("06/06/2008", "VIREMENT VERS EPARGNE", -100.00, "To account epargne", -100.00, -100.00, "Account n. 00001123")
      .check();
  }

  public void testImportOnSavingWithExternalSeries() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/08/06", -100.00, "ope")
      .load();

    accounts.createSavingsAccount("Virt Epargne", 100.00);

    views.selectBudget();
    budgetView.transfer.createSeries()
      .setName("CAF")
      .setFromAccount("External account")
      .setToAccount("Virt Epargne")
      .selectAllMonths()
      .setAmount(200)
      .validate();

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2008", "Planned: CAF", 200.00, "CAF", 700.00, 700.00, "Virt Epargne")
      .add("11/09/2008", "Planned: CAF", 200.00, "CAF", 500.00, 500.00, "Virt Epargne")
      .add("11/08/2008", "Planned: CAF", 200.00, "CAF", 300.00, 300.00, "Virt Epargne")
      .add("06/08/2008", "OPE", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .check();

    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/07/06", 100.00, "Alloc")
      .addTransaction("2008/08/06", 100.00, "Alloc")
      .load("Account n. 111", "Virt Epargne");

    savingsAccounts.changePosition("Virt Epargne", 3000.00, "");
    timeline.selectAll();
    transactions.initAmountContent()
      .add("11/10/2008", "Planned: CAF", 200.00, "CAF", 3600.00, 3600.00, "Virt Epargne")
      .add("11/09/2008", "Planned: CAF", 200.00, "CAF", 3400.00, 3400.00, "Virt Epargne")
      .add("11/08/2008", "Planned: CAF", 200.00, "CAF", 3200.00, 3200.00, "Virt Epargne")
      .add("06/08/2008", "ALLOC", 100.00, "To categorize", 3000.00, 3000.00, "Virt Epargne")
      .add("06/08/2008", "OPE", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .add("06/07/2008", "ALLOC", 100.00, "To categorize", 2900.00, 2900.00, "Virt Epargne")
      .check();
  }

  public void testImportOfxOnExistingAccountWithouAccountDoNotAskForAmount() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/08/06", -100.00, "ope")
      .load();

    accounts.createSavingsAccount("Virt Epargne", null);

    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/07/06", 100.00, "Alloc")
      .addTransaction("2008/08/06", 100.00, "Alloc")
      .load("Account n. 111", "Virt Epargne");

    transactions.initContent()
      .add("06/08/2008", TransactionType.VIREMENT, "ALLOC", "", 100.00)
      .add("06/08/2008", TransactionType.PRELEVEMENT, "OPE", "", -100.00)
      .check();
  }

  public void testSavingsWithoutAccountPosition() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/08/06", -100.00, "Savings")
      .load();

    accounts.createNewAccount()
      .setName("Epargne")
      .setAccountNumber("1234")
      .selectBank("LCL")
      .setAsSavings()
      .setStartDate("2005/12/01")
      .validate();
    savingsAccounts.checkPosition("Epargne", 0.00);

    categorization.selectTransaction("Savings")
      .selectTransfers()
      .selectAndCreateTransferSeries("Virt Epargne", "Account n. 00001123", "Epargne", 100.00);
    budgetView.transfer.checkContent("| Virt Epargne | 100.00 | 100.00 |");
    savingsAccounts.checkPosition("Epargne", 0.00);

    savingsAccounts.editPosition("Epargne")
      .setAmount(200.00)
      .validate();
    savingsAccounts.checkPosition("Epargne", 200.00);

    QifBuilder.init(this)
      .addTransaction("2008/08/15", -100.00, "Savings 1")
      .loadInAccount("Epargne");
    savingsAccounts.checkPosition("Epargne", 100.00);

    setCurrentDate("2008/09/03");
    restartApplicationFromBackup();

    OfxBuilder.init(this)
      .addTransaction("2008/08/31", -100.00, "Savings 2")  // devrait impacter le solde mais on ne sait pas faire
      .addTransaction("2008/09/02", -100.00, "Savings 3")  // l'operation est le meme jour que le solde du compte
      .loadInAccount("Epargne");

    savingsAccounts.checkPosition("Epargne", -100.00);
  }

  public void testChangeAmountOfEmptyAccount() throws Exception {
    accounts.createSavingsAccount("Virt Epargne", 100.);

    savingsAccounts.editPosition("Virt Epargne")
      .setAmount(250.)
      .validate();

    savingsAccounts.checkPosition("Virt Epargne", 250.);

  }
}
