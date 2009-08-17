package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Month;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class BudgetSummaryDetailsChecker extends GuiChecker {
  private Window window;

  public BudgetSummaryDetailsChecker(Window window) {
    this.window = window;
  }

  public BudgetSummaryDetailsChecker checkTitle(String title) {
    assertThat(window.getTextBox("title").textEquals(title));
    return this;
  }

  public BudgetSummaryDetailsChecker checkPosition(double amount) {
    TextBox textBox = window.getTextBox("estimatedPosition");
    assertThat(textBox.textEquals(toString(amount, false)));
    return this;
  }

  public BudgetSummaryDetailsChecker checkFuturePositionLabel(int monthId) {
    TextBox textBox = window.getTextBox("estimatedPositionDate");
    assertThat(textBox.textEquals("Estimated balance at " + Formatting.toString(Month.getLastDay(monthId))));
    return this;
  }

  public BudgetSummaryDetailsChecker checkPositionDate(String date) {
    return checkPositionDateText("on " + date);
  }

  public BudgetSummaryDetailsChecker checkPositionDate(int monthId) {
    TextBox textBox = window.getTextBox("estimatedPositionDate");
    assertThat(textBox.textEquals("on " + Formatting.toString(Month.getLastDay(monthId))));
    return this;
  }

  public BudgetSummaryDetailsChecker checkPositionDateText(String expected) {
    TextBox textBox = window.getTextBox("estimatedPositionDate");
    assertThat(textBox.textEquals(expected));
    return this;
  }

  public BudgetSummaryDetailsChecker checkPositionDescriptionContains(String text) {
    assertThat(window.getTextBox("positionDescription").textContains(text));
    return this;
  }

  public BudgetSummaryDetailsChecker checkInitialPosition(double amount) {
    TextBox textBox = window.getTextBox("initialPosition");
    assertThat(textBox.textEquals(toString(amount, false)));
    return this;
  }

  public BudgetSummaryDetailsChecker checkIncome(double amount) {
    return check(amount, "remainingIncome");
  }

  public BudgetSummaryDetailsChecker checkFixed(double amount) {
    return check(amount, "remainingFixed");
  }

  public BudgetSummaryDetailsChecker checkEnvelope(double amount) {
    return check(amount, "remainingEnvelope");
  }

  public BudgetSummaryDetailsChecker checkSavingsIn(double amount) {
    return check(amount, "remainingInSavings");
  }

  public BudgetSummaryDetailsChecker checkSavingsOut(double amount) {
    return check(amount, "remainingOutSavings");
  }

  public BudgetSummaryDetailsChecker checkOccasional(double amount) {
    return check(amount, "remainingOccasional");
  }

  public BudgetSummaryDetailsChecker checkProjects(double amount) {
    return check(amount, "remainingSpecial");
  }

  public BudgetSummaryDetailsChecker checkNoPositionDetails() {
    String[] names = {"initialPosition, remainingIncome", "remainingEnvelopes", "remaining"};
    for (String name : names) {
      checkComponentVisible(window, JLabel.class, name, false);
    }
    return this;
  }

  private BudgetSummaryDetailsChecker check(double amount, String name) {
    TextBox textBox = window.getTextBox(name);
    assertThat(textBox.textEquals(toString(amount)));
    return this;
  }

  public BudgetSummaryDetailsChecker checkBalance(double balance) {
    TextBox textBox = window.getTextBox("balanceLabel");
    assertThat(textBox.textEquals(toString(balance, true)));
    return this;
  }

  public BudgetSummaryDetailsChecker checkBalanceDetails(double income, double recurring, double envelopes, double savings, double special) {
    StackChart chart = (StackChart)window.getPanel("balanceChart").getAwtComponent();

    StackChartDataset incomeDataset = chart.getLeftDataset();
    Assert.assertEquals("Actual income: " + incomeDataset, 1, incomeDataset.size());
    Assert.assertEquals("Actual income: " + incomeDataset, income, incomeDataset.getValue(0));

    Map<String, Double> expected = new HashMap<String, Double>();
    expected.put("Envelopes", envelopes);
    expected.put("Recurring", recurring);
    expected.put("Savings", savings);
    expected.put("Special", special);

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

  public void close() {
    window.getButton("Close").click();
  }
}