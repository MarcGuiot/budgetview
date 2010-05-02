package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class BudgetChecker extends GuiChecker {
  private LoggedInFunctionalTestCase testCase;

  public BudgetChecker(LoggedInFunctionalTestCase testCase) {
    this.testCase = testCase;
  }

  public BudgetChecker checkLastBankAmount(double amount) {
    testCase.views.selectBudget();
    testCase.budgetView.getSummary()
      .openPositionPanel()
      .checkInitialPosition(amount)
      .close();
    return this;
  }

  public BudgetChecker checkIncome(double amount) {
    testCase.views.selectBudget();
    testCase.budgetView.income.checkTotalPlanned(amount);
    return this;
  }

  public BudgetChecker checkRecurring(double amount) {
    testCase.views.selectBudget();
    testCase.budgetView.recurring.checkTotalPlanned(amount);
    return this;
  }

  public BudgetChecker checkExpense(double amount) {
    testCase.views.selectBudget();
    testCase.budgetView.getSummary()
      .openPositionPanel()
      .checkExpense(amount)
      .close();
    return this;
  }

  public BudgetChecker checkSavings(double amount) {
    testCase.views.selectBudget();
    testCase.budgetView.savings.checkTotalPlanned(amount);
    return this;
  }

  public BudgetChecker checkVariable(double amount) {
    testCase.views.selectBudget();
    testCase.budgetView.variable.checkTotalPlanned(amount);
    return this;
  }

  public BudgetChecker checkSavingsIn(double amount) {
    testCase.views.selectBudget();
    testCase.budgetView.getSummary()
      .openPositionPanel()
      .checkSavingsIn(amount)
      .close();
    return this;
  }

  public BudgetChecker checkSavingsOut(double amount) {
    testCase.views.selectBudget();
    testCase.budgetView.getSummary()
      .openPositionPanel()
      .checkSavingsOut(amount)
      .close();
    return this;
  }

  public BudgetChecker checkExtras(double amount) {
    testCase.views.selectBudget();
    testCase.budgetView.extras.checkTotalPlanned(amount);
    return this;
  }

}
