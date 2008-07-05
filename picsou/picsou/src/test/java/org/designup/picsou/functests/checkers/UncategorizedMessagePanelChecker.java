package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;

public class UncategorizedMessagePanelChecker extends DataChecker {
  private Window window;

  public UncategorizedMessagePanelChecker(Window window) {
    this.window = window;
  }

  public void assertWarningIsDisplayed() {
    assertTrue(getWarningLabel().isVisible());
    assertTrue(getWarningLabel().textEquals(Lang.get("transaction.allocation.warning")));
  }

  public void assertNoWarningIsDisplayed() {
    assertFalse(getWarningLabel().isVisible());
  }

  private TextBox getWarningLabel() {
    return window.getTextBox("uncategorizedMessage");
  }
}
