package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Month;
import org.uispec4j.TextBox;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class BudgetWizardPositionChecker extends BudgetWizardPageChecker {

  public BudgetWizardPositionChecker(BudgetWizardPageChecker page) {
    super(page.gotoPage("End of month position"));
  }

  public BudgetWizardPositionChecker checkPosition(double amount) {
    TextBox textBox = panel.getTextBox("estimatedPosition");
    assertThat(textBox.textEquals(toString(amount, false)));
    return this;
  }

  public BudgetWizardPositionChecker checkFuturePositionLabel(int monthId) {
    TextBox textBox = panel.getTextBox("estimatedPositionDate");
    assertThat(textBox.textEquals("Estimated balance at " + Formatting.toString(Month.getLastDay(monthId))));
    return this;
  }

  public BudgetWizardPositionChecker checkPositionDate(String date) {
    return checkPositionDateText("on " + date);
  }

  public BudgetWizardPositionChecker checkPositionDate(int monthId) {
    TextBox textBox = panel.getTextBox("estimatedPositionDate");
    assertThat(textBox.textEquals("on " + Formatting.toString(Month.getLastDay(monthId))));
    return this;
  }

  public BudgetWizardPositionChecker checkPositionDateText(String expected) {
    TextBox textBox = panel.getTextBox("estimatedPositionDate");
    assertThat(textBox.textEquals(expected));
    return this;
  }

  public BudgetWizardPositionChecker checkPositionDescriptionContains(String text) {
    assertThat(panel.getTextBox("positionDescription").textContains(text));
    return this;
  }

  public BudgetWizardPositionChecker checkInitialPosition(double amount) {
    TextBox textBox = panel.getTextBox("initialPosition");
    assertThat(textBox.textEquals(toString(amount, false)));
    return this;
  }

  public BudgetWizardPositionChecker checkIncome(double amount) {
    return check(amount, "remainingIncome");
  }

  public BudgetWizardPositionChecker checkFixed(double amount) {
    return check(amount, "remainingFixed");
  }

  public BudgetWizardPositionChecker checkEnvelope(double amount) {
    return check(amount, "remainingEnvelope");
  }

  public BudgetWizardPositionChecker checkSavingsIn(double amount) {
    return check(amount, "remainingInSavings");
  }

  public BudgetWizardPositionChecker checkSavingsOut(double amount) {
    return check(amount, "remainingOutSavings");
  }

  public BudgetWizardPositionChecker checkExtras(double amount) {
    return check(amount, "remainingExtras");
  }

  public BudgetWizardPositionChecker checkNoPositionDetails() {
    String[] names = {"initialPosition, remainingIncome", "remainingEnvelopes", "remaining"};
    for (String name : names) {
      checkComponentVisible(panel, JLabel.class, name, false);
    }
    return this;
  }

  private BudgetWizardPositionChecker check(double amount, String name) {
    TextBox textBox = panel.getTextBox(name);
    assertThat(textBox.textEquals(toString(amount)));
    return this;
  }

  public BudgetWizardPositionChecker checkBalance(double balance) {
    TextBox textBox = panel.getTextBox("balanceLabel");
    assertThat(textBox.textEquals(toString(balance, true)));
    return this;
  }

  public BudgetWizardPositionChecker checkBalanceText(String text) {
    TextBox textBox = panel.getTextBox("balanceDescription");
    assertThat(textBox.textContains(text));
    return this;
  }

  public BudgetWizardPositionChecker checkBalanceDetails(double income, double recurring, double envelopes, double savings, double extras) {
    StackChart chart = (StackChart)panel.getPanel("balanceChart").getAwtComponent();

    StackChartDataset incomeDataset = chart.getLeftDataset();
    Assert.assertEquals("Actual income: " + incomeDataset, 1, incomeDataset.size());
    Assert.assertEquals("Actual income: " + incomeDataset, income, incomeDataset.getValue(0));

    Map<String, Double> expected = new HashMap<String, Double>();
    expected.put("Envelopes", envelopes);
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