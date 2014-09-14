package org.designup.picsou.functests.dashboard;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class DashboardTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2014/08/12");
    super.setUp();
  }

  public void test() throws Exception {

    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 250.0, "2014/08/12")
      .addTransaction("2014/07/28", 3000.00, "WorldCo")
      .addTransaction("2014/08/05", -100.00, "Auchan")
      .addTransaction("2014/08/09", -200.00, "FNAC")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000234", 1000.0, "2014/08/12")
      .addTransaction("2014/07/28", -50.00, "Free")
      .load();

    dashboard.checkContent(
      "| 0     | Days since your last import                                       |\n" +
      "| 4     | Transactions to categorize                                        |\n" +
      "| sunny | No overdraw forecast for your accounts until the end of September |\n" +
      "| +1250 | Available on you main accounts until the end of September         |\n" +
      "| +1250 | Total amount for your main accounts on 2014/08/09                 |\n");

    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewRecurring("Free", "Internet");
    categorization.setNewVariable("Auchan", "Groceries", -500.00);
    categorization.setNewVariable("FNAC", "Leisures", -200.00);

    dashboard.checkContent(
      "| 0     | Days since your last import                                               |\n" +
      "| OK    | All your transactions are categorized                                     |\n" +
      "| rainy | Important overdraw forecast for your accounts before the end of September |\n" +
      "| +750  | Available on you main accounts until the end of September                 |\n" +
      "| +1250 | Total amount for your main accounts on 2014/08/09                         |\n");

    mainAccounts.editPosition("Account n. 000123")
      .setAmount(100.00)
      .validate();

    dashboard.checkContent("| 0     | Days since your last import                                               |\n" +
                           "| OK    | All your transactions are categorized                                     |\n" +
                           "| rainy | Important overdraw forecast for your accounts before the end of September |\n" +
                           "| +600  | Available on you main accounts until the end of September                 |\n" +
                           "| +1100 | Total amount for your main accounts on 2014/08/09                         |\n");

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000345", 5000.0, "2014/08/15")
      .addTransaction("2014/07/28", +100.00, "Virt")
      .load();
    mainAccounts.edit("Account n. 000345")
      .setAsSavings()
      .validate();

    dashboard.checkContent(
      "| 0     | Days since your last import                                               |\n" +
      "| 1     | Transactions to categorize                                                |\n" +
      "| rainy | Important overdraw forecast for your accounts before the end of September |\n" +
      "| +600  | Available on you main accounts until the end of September                 |\n" +
      "| +1100 | Total amount for your main accounts on 2014/08/09                         |\n" +
      "| +6100 | Total amount for all your accounts on 2014/07/28                          |\n");

    timeline.selectMonth("2014/08");
    budgetView.savings.createSeries()
      .setName("Transfer")
      .setFromAccount("Account n. 000345")
      .setToAccount("Account n. 000123")
      .setAmount(250.00)
      .setForceSingleOperationDay(10)
      .validate();

    dashboard.checkContent(
      "| 0      | Days since your last import                                           |\n" +
      "| 1      | Transactions to categorize                                            |\n" +
      "| cloudy | Light overdraw forecast for your accounts before the end of September |\n" +
      "| +850   | Available on you main accounts until the end of September             |\n" +
      "| +1100  | Total amount for your main accounts on 2014/08/09                     |\n" +
      "| +6100  | Total amount for all your accounts on 2014/07/28                      |\n");

    budgetView.savings.editPlannedAmount("Transfer")
      .setAmount(500.00)
      .validate();

    dashboard.checkContent(
      "| 0     | Days since your last import                                       |\n" +
      "| 1     | Transactions to categorize                                        |\n" +
      "| sunny | No overdraw forecast for your accounts until the end of September |\n" +
      "| +1100 | Available on you main accounts until the end of September         |\n" +
      "| +1100 | Total amount for your main accounts on 2014/08/09                 |\n" +
      "| +6100 | Total amount for all your accounts on 2014/07/28                  |\n");
  }

  public void testDaysSinceLastImport() throws Exception {
    fail("positions de comptes: quelle position/date choisir dans Account? Mise à jour en cas de saisie manuelle ?");
    fail("changement de mois avec appli ouverte");
  }

  public void testNothingShownBeforeFirstAccountIsCreated() throws Exception {
    fail("tbd");
  }

  public void testTODO() throws Exception {
    fail("cas où il n'y a pas d'opérations prévues - sunny?");
  }

  public void testAccountFiltering() throws Exception {
    fail("tbd");
  }
}
