package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.designup.picsou.gui.description.Formatting;
import org.uispec4j.TextBox;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

import java.util.HashMap;
import java.util.Map;

public class BudgetWizardBalanceChecker extends BudgetWizardPageChecker {

  public BudgetWizardBalanceChecker(BudgetWizardPageChecker wizardPage) {
    super(wizardPage.gotoPage("Balance"));
  }

  public BudgetWizardBalanceChecker checkBalance(double balance) {
    TextBox textBox = panel.getTextBox("balanceAmount");
    assertThat(textBox.textContains(Formatting.toString(balance)));
    return this;
  }

  public BudgetWizardBalanceChecker checkBalanceDetails(double income, double recurring, double variable, double savings, double extras) {
    StackChart chart = (StackChart)panel.getPanel("balanceChart").getAwtComponent();

    StackChartDataset incomeDataset = chart.getLeftDataset();
    Assert.assertEquals("Actual income: " + incomeDataset, 1, incomeDataset.size());
    Assert.assertEquals("Actual income: " + incomeDataset, income, incomeDataset.getValue(0));

    Map<String, Double> expected = new HashMap<String, Double>();
    expected.put("Variable", variable);
    expected.put("Recurring", recurring);
    expected.put("Savings", savings);
    expected.put("Extras", extras);

    StackChartDataset expensesDataset = chart.getRightDataset();
    for (int i = 0; i < expensesDataset.size(); i++) {
      String label = expensesDataset.getLabel(i);
      Double expectedValue = expected.get(label);
      if (expectedValue == null) {
        Assert.fail("Unexpected " + label + " in dataset");
      }
      Assert.assertEquals("Actual expenses: " + expensesDataset, expectedValue, expensesDataset.getValue(i), 0.01);
    }

    return this;
  }
}