package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.UISpecAssert;

public class BudgetViewChecker extends DataChecker {

  public final BudgetAreaChecker income;
  public final BudgetAreaChecker recurring;
  public final BudgetAreaChecker envelopes;

  private Window window;

  public BudgetViewChecker(Window window) {
    this.window = window;
    this.income = new BudgetAreaChecker("incomeBudgetView");
    this.recurring = new BudgetAreaChecker("recurringBudgetView");
    this.envelopes = new BudgetAreaChecker("envelopeBudgetView");
  }

  public class BudgetAreaChecker {
    private String panelName;

    public BudgetAreaChecker(String panelName) {
      this.panelName = panelName;
    }

    public void checkTitle(String title) {
      Panel budgetPanel = window.getPanel(panelName);
      TextBox label = budgetPanel.getTextBox("budgetAreaTitle");
      UISpecAssert.assertThat(label.textEquals(title));
    }

    public void checkTotalAmounts(double received, int spent) {
      Panel budgetPanel = window.getPanel(panelName);
      TextBox totalObserved = budgetPanel.getTextBox("totalObservedAmount");
      UISpecAssert.assertTrue(totalObserved.textEquals(BudgetViewChecker.this.toString(received)));

      TextBox totalPlanned = budgetPanel.getTextBox("totalPlannedAmount");
      UISpecAssert.assertTrue(totalPlanned.textEquals(BudgetViewChecker.this.toString(spent)));
    }

    public void checkSeries(String seriesName, double observedAmount, double plannedAmount) {
      Panel budgetPanel = window.getPanel(panelName);
      TextBox nameBox = budgetPanel.getTextBox(seriesName);
      Panel seriesRow = nameBox.getContainer("seriesRow");
      TextBox observedLabel = seriesRow.getTextBox("observedSeriesAmount");
      UISpecAssert.assertTrue(observedLabel.textEquals(BudgetViewChecker.this.toString(observedAmount)));
      TextBox plannedLabel = seriesRow.getTextBox("plannedSeriesAmount");
      UISpecAssert.assertTrue(plannedLabel.textEquals(BudgetViewChecker.this.toString(plannedAmount)));
    }
  }
}
