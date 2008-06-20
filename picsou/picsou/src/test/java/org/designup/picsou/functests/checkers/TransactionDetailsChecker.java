package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.TextBox;
import org.uispec4j.Panel;
import org.uispec4j.finder.ComponentMatcher;
import org.uispec4j.finder.ComponentMatchers;
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
    assertThat(getPanel().getTextBox("label").textEquals(expected));
  }

  public void checkDate(String expected) {
    assertThat(getPanel().getTextBox("date").textEquals(expected));
  }

  public void checkNoDate() {
    UISpecAssert.assertFalse(getPanel().getTextBox("date").isVisible());
  }
}
