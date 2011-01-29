package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.*;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class MainAccountViewChecker extends AccountViewChecker<MainAccountViewChecker> {

  private BudgetViewChecker budget;

  public MainAccountViewChecker(Window window) {
    super(window, "mainAccountView");
  }

  public MainAccountViewChecker checkEstimatedPosition(double amount) {
    getBudgetSummary().checkEndPosition(amount);
    return this;
  }

  public MainAccountViewChecker checkNoEstimatedPosition() {
    getBudgetSummary().checkNoEstimatedPosition();
    return this;
  }

  public MainAccountViewChecker checkEstimatedPositionDate(String text) {
    Assert.fail("TBD");
    return this;
  }

  protected BudgetSummaryViewChecker getBudgetSummary() {
    if (budget == null) {
      views.selectBudget();
      budget = new BudgetViewChecker(mainWindow);
    }
    return budget.getSummary();
  }


}
