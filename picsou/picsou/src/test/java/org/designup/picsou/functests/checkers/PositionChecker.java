package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.assertion.UISpecAssert;
import org.designup.picsou.gui.description.Formatting;

public class PositionChecker extends GuiChecker{
  private Window window;

  public PositionChecker(Window window) {
    this.window = window;
  }

  public PositionChecker checkPast(double amount){
    UISpecAssert.assertThat(window.getTextBox("positionPastAmount").textContains(Formatting.toString(amount)));
    return this;
  }

  public PositionChecker checkPresent(double amount, double income, double expence, double savings, double enfOfMonth){
    UISpecAssert.assertThat(window.getTextBox("bankPositionAmount").textContains(Formatting.toString(amount)));
    UISpecAssert.assertThat(window.getTextBox("waitedIncomeAmount").textContains(Formatting.toString(income)));
    UISpecAssert.assertThat(window.getTextBox("waitedExpenseAmount").textContains(Formatting.toString(expence)));
    UISpecAssert.assertThat(window.getTextBox("waitedSavingsAmount").textContains(Formatting.toString(savings)));
    UISpecAssert.assertThat(window.getTextBox("estimatedPosition").textContains(Formatting.toString(enfOfMonth)));
    return this;
  }

  public PositionChecker checkThreshold(double threshold){
    UISpecAssert.assertThat(window.getTextBox("positionPanelLimit").textContains(Formatting.toString(threshold)));
    return this;
  }

  public PositionChecker checkBalanceZeroWithSavings(){
    UISpecAssert.assertThat(window.getTextBox("positionPanelLimit")
      .textContains("Can you reduce your expence to enhance your savings"));
    return this;
  }

  public PositionChecker checkBalanceZeroWithoutSavings(){
    UISpecAssert.assertThat(window.getTextBox("positionPanelLimit")
      .textContains("Can you reduce your expence to open a savings account"));
    return this;
  }

  public PositionChecker checkSavingsExpected(){
    UISpecAssert.assertThat(window.getTextBox("positionPanelLimit")
      .textContains("You can to more savings"));
    return this;
  }

  public PositionChecker checkOpenSavings(){
    UISpecAssert.assertThat(window.getTextBox("positionPanelLimit")
      .textContains("Open a savings account"));
    return this;
  }

  public PositionChecker checkTooMuchExpence(){
    UISpecAssert.assertThat(window.getTextBox("positionPanelLimit")
      .textContains("You should reduce your expence"));
    return this;
  }


  public PositionChecker changeThreshold(double threshold){
    AccountPositionThresholdChecker thresholdChecker =
      new AccountPositionThresholdChecker(
        WindowInterceptor.getModalDialog(window.getTextBox("positionPanelLimit").triggerClickOnHyperlink("alerte")));
    thresholdChecker.setAmountAndClose(threshold);
    return this;
  }

  public void close() {
    window.getButton().click();
    UISpecAssert.assertFalse(window.isVisible());
  }
}
