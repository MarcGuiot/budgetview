package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.Panel;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class TransactionDetailsChecker extends DataChecker {
  private Window window;

  public TransactionDetailsChecker(Window window) {
    this.window = window;
  }

  private Panel getPanel() {
    return window.getPanel("transactionDetails");
  }

  public void checkLabel(String expected) {
    checkValue("label", expected);
  }

  public void checkDate(String expected) {
    checkValue("date", expected);
  }

  public void checkNoDate() {
    checkNotVisible("date");
  }

  public void checkAmount(String label, String amount) {
    checkValue("amountLabel", label);
    checkValue("amountValue", amount);
  }

  public void checkNoAmount() {
    checkNotVisible("amountLabel");
    checkNotVisible("amountValue");
  }

  public void checkAmountStatistics(String minAmount,
                                    String maxAmount,
                                    String averageAmount) {
    UISpecAssert.assertTrue(getPanel().getPanel("amountPanel").isVisible());
    checkValue("minimumAmount", minAmount);
    checkValue("maximumAmount", maxAmount);
    checkValue("averageAmount", averageAmount);
  }

  public void checkNoAmountStatistics() {
    UISpecAssert.assertFalse(getPanel().getPanel("amountPanel").isVisible());
  }

  private void checkValue(String name, String label) {
    assertThat(getPanel().getTextBox(name).textEquals(label));
  }

  private void checkNotVisible(String name) {
    UISpecAssert.assertFalse(getPanel().getTextBox(name).isVisible());
  }
}
