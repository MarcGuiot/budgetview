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

  public BudgetWizardPositionChecker checkPositionDateText(String expected) {
    TextBox textBox = panel.getTextBox("estimatedPositionDate");
    assertThat(textBox.textEquals(expected));
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

  public BudgetWizardPositionChecker checkVariable(double amount) {
    return check(amount, "remainingVariable");
  }

  public BudgetWizardPositionChecker checkSavingsIn(double amount) {
    return check(amount, "remainingInSavings");
  }

  public BudgetWizardPositionChecker checkSavingsOut(double amount) {
    return check(amount, "remainingOutSavings");
  }

  private BudgetWizardPositionChecker check(double amount, String name) {
    TextBox textBox = panel.getTextBox(name);
    assertThat(textBox.textEquals(toString(amount)));
    return this;
  }

}