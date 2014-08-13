package org.designup.picsou.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.Window;

import static org.uispec4j.assertion.UISpecAssert.*;

public class UncategorizedSummaryViewChecker extends ViewChecker {
  private Panel panel;

  public UncategorizedSummaryViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public UncategorizedSummaryViewChecker checkAmount(double amount) {
    Panel uncategorizedPanel = getPanel();
    assertThat(uncategorizedPanel.isVisible());
    Button button = uncategorizedPanel.getButton("uncategorized");
    assertThat(button.textEquals(toString(amount, false)));
    assertThat(button.isEnabled());
    return this;
  }

  public UncategorizedSummaryViewChecker checkNotShown() {
    assertFalse(getPanel().isVisible());
    return this;
  }

  public void gotoUncategorized() {
    getPanel().getButton("uncategorized").click();
  }

  private Panel getPanel() {
    if (panel == null) {
      panel = mainWindow.getPanel("uncategorizedSummaryView");
    }
    return panel;
  }
}
