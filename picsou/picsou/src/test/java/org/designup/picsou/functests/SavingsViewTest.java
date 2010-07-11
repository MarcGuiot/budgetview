package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SavingsViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2009/07");
    super.setUp();
  }

  public void testEditingSavingsSeries() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2009/07/30")
      .addTransaction("2009/07/10", -200.00, "Virt")
      .load();

    savingsAccounts
      .createNewAccount()
      .setAccountName("ING")
      .selectBank("ING Direct")
      .setPosition(200)
      .validate();

    views.selectCategorization();
    categorization.setNewSavings("Virt", "Epargne", "Main accounts", "ING");

    views.selectSavings();
    savingsView.checkContainsSeries("ING", "Epargne");
    savingsView.checkSeriesAmounts("ING", "Epargne", 200, 200);
    savingsView.editPlannedAmount("ING", "Epargne")
      .checkAmountsRadioAreNotVisible()
      .setAmount(300)
      .validate();
    savingsView.checkSeriesAmounts("ING", "Epargne", 200, 300);
  }

  public void testSavingsAccountsEvolution() throws Exception {

    operations.openPreferences().setFutureMonthsCount(12).validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2009/07/30")
      .addTransaction("2009/07/10", -200.00, "Virt")
      .load();

    savingsAccounts
      .createNewAccount()
      .setAccountName("ING")
      .selectBank("ING Direct")
      .setPosition(200)
      .validate();

    views.selectCategorization();
    categorization.setNewSavings("Virt", "Epargne", "Main accounts", "ING");

    views.selectSavings();
    savingsView.histoChart
      .checkColumnCount(13)
      .checkLineColumn(0, "J", "2009", 200.00, true)
      .checkLineColumn(1, "A", "2009", 400.00)
      .checkLineColumn(2, "S", "2009", 600.00)
      .checkLineColumn(3, "O", "2009", 800.00)
      .checkLineColumn(6, "J", "2010", 1400.00)
      .checkLineColumn(12, "J", "2010", 2600.00);
    savingsView.histoChart.checkTooltip(1, "End of August 2009 position: 400.00");

    views.selectHome();
    savingsAccounts.editPosition("ING").setAmount(300.00).validate();

    views.selectSavings();
    savingsView.histoChart
      .checkColumnCount(13)
      .checkLineColumn(0, "J", "2009", 300.00, true)
      .checkLineColumn(1, "A", "2009", 500.00)
      .checkLineColumn(2, "S", "2009", 700.00)
      .checkLineColumn(3, "O", "2009", 900.00)
      .checkLineColumn(6, "J", "2010", 1500.00)
      .checkLineColumn(12, "J", "2010", 2700.00);

    operations.openPreferences().setFutureMonthsCount(6).validate();

    views.selectSavings();
    savingsView.histoChart
      .checkColumnCount(7)
      .checkLineColumn(0, "J", "2009", 300.00, true)
      .checkLineColumn(1, "A", "2009", 500.00)
      .checkLineColumn(2, "S", "2009", 700.00)
      .checkLineColumn(3, "O", "2009", 900.00)
      .checkLineColumn(6, "J", "2010", 1500.00);
  }
}
