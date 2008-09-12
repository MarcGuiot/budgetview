package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.*;

public class UncategorizedMessagePanelChecker extends DataChecker {
  private Window window;

  public UncategorizedMessagePanelChecker(Window window) {
    this.window = window;
  }

  public void assertWarningIsDisplayed(int count) {
    assertTrue(getWarningLabel().isVisible());
    assertTrue(getWarningLabel().textEquals(Lang.get("transaction.allocation.warning", count)));
  }

  public void assertNoWarningIsDisplayed() {
    assertFalse(getWarningLabel().isVisible());
  }

  private TextBox getWarningLabel() {
    return window.getTextBox("uncategorizedMessage");
  }

  public void categorize() {
    window.getPanel("uncategorizedMessagePanel").getButton("Categorize").click();
  }
}
