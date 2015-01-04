package org.designup.picsou.functests.addons;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.RestartTestCase;

public class AddOnsTest extends RestartTestCase {

  protected String getCurrentDate() {
    return "2015/01/15";
  }

  protected void setUp() throws Exception {
    setCurrentMonth("2015/01");
    super.setUp();
    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(2).validate();
  }

  public void testAccessingProjectsWhenTheAddOnIsDisabled() throws Exception {

    addOns.activateProjects();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2015/01/30")
      .addTransaction("2015/01/01", 1000.00, "Income")
      .addTransaction("2015/01/15", -150.00, "Resa")
      .load();

    projects.createFirst();
    currentProject.setNameAndValidate("MyProject");
    currentProject
      .addExpenseItem(0, "Booking", 201501, -200.00)
      .addExpenseItem(1, "Travel", 201502, -100.00)
      .addExpenseItem(2, "Hotel", 201502, -500.00);
    categorization.selectTransaction("RESA")
      .selectExtras().selectSeries("Booking");

    addOns.disableProjects();
    views.checkProjectsDisabled();

    views.selectBudget();
    budgetView.extras.checkProjectsDisabledMessage("MyProject");
    views.checkBudgetSelected();

    budgetView.extras.expandGroup("MyProject");
    budgetView.extras.checkProjectsDisabledMessage("Booking");
    views.checkBudgetSelected();
  }

  public void testAnalysisActionsHiddenWhenAddOnIsDisabled() throws Exception {
    addOns.activateAnalysis();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2015/01/30")
      .addTransaction("2015/01/01", 1000.00, "Income")
      .addTransaction("2015/01/15", -150.00, "Auchan")
      .addTransaction("2015/01/15", -150.00, "McDo")
      .load();

    categorization.setNewIncome("INCOME", "Salary", 1000.00);
    categorization.setNewVariable("AUCHAN", "Groceries", 300.00);
    categorization.setNewVariable("MCDO", "Restaurant", 300.00);
    budgetView.variable.addToNewGroup("Groceries", "Food");
    budgetView.variable.addToGroup("Restaurant", "Food");

    views.checkAnalysisEnabled();
    budgetView.income.checkGotoAnalysisShown("Salary");
    budgetView.variable.checkGotoAnalysisShown("Food");
    budgetView.variable.checkGotoAnalysisShown("Groceries");

    addOns.disableAnalysis();

    views.checkAnalysisDisabled();
    budgetView.income.checkGotoAnalysisHidden("Salary");
    budgetView.variable.checkGotoAnalysisHidden("Food");
    budgetView.variable.checkGotoAnalysisHidden("Groceries");

    budgetView.variable.gotoDataThroughMenu("Food");
    transactions.initAmountContent()
      .add("15/01/2015", "MCDO", -150.00, "Restaurant", 1000.00, 1000.00, "Account n. 001111")
      .add("15/01/2015", "AUCHAN", -150.00, "Groceries", 1150.00, 1150.00, "Account n. 001111")
      .check();
  }
}
