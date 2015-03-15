package org.designup.picsou.functests.budget;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class BudgetViewEditionTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2008/08");
    super.setUp();
  }

  public void testCreateAndDeleteManySeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", -29.00, "Auchan")
      .addTransaction("2008/06/29", -60.00, "ELF")
      .load();

    timeline.selectMonths("2008/06", "2008/07");

    views.selectBudget();
    budgetView.recurring.createSeries()
      .setName("Groceries")
      .validate();
    budgetView.recurring.createSeries()
      .setName("Fuel")
      .validate();

    budgetView.recurring.checkSeries("Groceries", 0, 0);
    budgetView.recurring.checkSeries("Fuel", 0, 0);

    budgetView.recurring.editSeries("Groceries")
      .selectMonth(200807)
      .setAmount("200")
      .validate();

    budgetView.recurring.checkSeries("Groceries", 0.00, -200.00);
    budgetView.recurring.checkSeries("Fuel", 0, 0);

    views.selectCategorization();
    categorization.selectTransactions("ELF")
      .selectRecurring()
      .selectSeries("Fuel");

    categorization.selectTransactions("Auchan")
      .selectRecurring()
      .selectSeries("Groceries");

    views.selectBudget();
    budgetView.recurring.checkSeries("Fuel", -60, -120);

    SeriesEditionDialogChecker editionDialogChecker = budgetView.recurring.editSeries("Groceries");
    editionDialogChecker
      .deleteCurrentSeriesWithConfirmation();
    editionDialogChecker.validate();
    budgetView.recurring.checkSeriesNotPresent("Groceries");

    transactions.initAmountContent()
      .add("29/07/2008", "AUCHAN", -29.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .add("29/06/2008", "ELF", -60.00, "Fuel", 29.00, 29.00, "Account n. 00001123")
      .check();

    uncategorized.checkAmountAndTransactions(29.00, "| 29/07/2008 |  | AUCHAN | -29.00 |\n");
  }

  public void testEditingASeriesWithTransactions() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectData();
    transactions.initContent()
      .add("29/07/2008", "01/08/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .check();

    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Internet");

    views.selectBudget();

    budgetView.recurring.editSeries("Internet")
      .checkTitle("Editing a series")
      .checkName("Internet")
      .checkBudgetArea("Recurring")
      .setName("Free")
      .validate();

    budgetView.recurring.checkSeries("Free", -29.00, -29.00);
  }

  public void testEditingASeriesAmountHasNoImpactOnOtherSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Auchan")
      .addTransaction("2008/06/29", "2008/08/01", -29.00, "Auchan")
      .load();

    timeline.selectMonth("2008/07");

    views.selectBudget();
    budgetView.recurring.createSeries()
      .setName("Groceries")
      .validate();
    budgetView.recurring.createSeries()
      .setName("Fuel")
      .validate();

    budgetView.recurring.checkSeries("Groceries", 0, 0);
    budgetView.recurring.checkSeries("Fuel", 0, 0);

    budgetView.recurring.editSeries("Groceries")
      .selectMonth(200807)
      .setAmount("200")
      .validate();

    budgetView.recurring.checkSeries("Groceries", 0.00, -200.00);
    budgetView.recurring.checkSeries("Fuel", 0, 0);
  }
}
