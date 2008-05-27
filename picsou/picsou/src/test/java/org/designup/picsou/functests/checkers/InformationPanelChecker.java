package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.designup.picsou.utils.Lang;

public class InformationPanelChecker extends DataChecker {
  private Panel panel;
  private TextBox warningLabel;

  public InformationPanelChecker(Window window) {
    panel = window.getPanel("informationPanel");
    warningLabel = panel.getTextBox();
  }

  public void assertWarningIsDisplayed() {
    warningLabel.textEquals(Lang.get("transaction.allocation.warning"));
  }

  public void assertNoWarningIsDisplayed() {
    warningLabel.textIsEmpty();
  }
}
