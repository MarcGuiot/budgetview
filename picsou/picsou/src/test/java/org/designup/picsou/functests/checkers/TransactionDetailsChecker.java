package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.TextBox;
import org.uispec4j.Panel;
import org.uispec4j.assertion.UISpecAssert;

public class TransactionDetailsChecker extends DataChecker {
  private Window window;

  public TransactionDetailsChecker(Window window) {
    this.window = window;
  }

  private Panel getPanel() {
    return window.getPanel("transactionDetails");
  }

  public void checkLabel(String expected) {
    UISpecAssert.assertThat(getPanel().getTextBox("label").textEquals(expected));
  }
}
