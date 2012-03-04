package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;

import javax.swing.*;

public class SignpostViewChecker extends ViewChecker {
  private Panel panel;

  public SignpostViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public SignpostViewChecker checkSignpostViewShown() {
    views.selectHome();
    checkComponentVisible(mainWindow, JPanel.class, "signpostView", true);
    checkComponentVisible(mainWindow, JPanel.class, "summaryView", false);
    return this;
  }

  public SignpostViewChecker checkSummaryViewShown() {
    views.selectHome();
    checkComponentVisible(mainWindow, JPanel.class, "signpostView", false);
    checkComponentVisible(mainWindow, JPanel.class, "summaryView", true);
    return this;
  }

  public void gotoDemoAccount() {
    getPanel().getButton("gotoDemoAccount").click();
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectHome();
      panel = mainWindow.getPanel("signpostView");
    }
    return panel;
  }
}
