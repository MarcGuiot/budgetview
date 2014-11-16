package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;

import javax.swing.*;

public class SignpostViewChecker extends ViewChecker {

  public SignpostViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public SignpostViewChecker checkSignpostViewShown() {
    views.selectHome();
    checkComponentVisible(mainWindow, JPanel.class, "signpostView", true);
    checkComponentVisible(mainWindow, JPanel.class, "dashboardView", false);
    return this;
  }

  public SignpostViewChecker checkDashboardViewShown() {
    views.selectHome();
    checkComponentVisible(mainWindow, JPanel.class, "signpostView", false);
    checkComponentVisible(mainWindow, JPanel.class, "dashboardView", true);
    return this;
  }
}
