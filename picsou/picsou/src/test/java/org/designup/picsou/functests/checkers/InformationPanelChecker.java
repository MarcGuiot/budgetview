package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;

public class InformationPanelChecker extends DataChecker {
  private TextBox warningLabel;

  public InformationPanelChecker(Window window) {
    warningLabel = window.getTextBox("uncategorizedLabel");
  }

  public void assertWarningIsDisplayed() {
    assertTrue(warningLabel.isVisible());
    assertTrue(warningLabel.textEquals(Lang.get("transaction.allocation.warning")));
  }

  public void assertNoWarningIsDisplayed() {
    assertFalse(warningLabel.isVisible());
  }
}
