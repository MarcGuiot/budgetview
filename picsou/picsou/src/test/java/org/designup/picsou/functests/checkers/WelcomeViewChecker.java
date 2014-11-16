package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Window;

import javax.swing.*;

public class WelcomeViewChecker extends ViewChecker {
  private Panel panel;

  public WelcomeViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public WelcomeViewChecker checkWelcomeViewShown() {
    views.selectHome();
    checkComponentVisible(mainWindow, JPanel.class, "welcomeView", true);
    checkComponentVisible(mainWindow, JPanel.class, "welcomeIntro", true);
    checkComponentVisible(mainWindow, JPanel.class, "signpostView", false);
    checkComponentVisible(mainWindow, JPanel.class, "dashboardView", false);
    return this;
  }

  public void gotoDemoAccount() {
    getPanel().getButton("demo").click();
  }


  public void start() {
    getPanel().getButton("start").click();
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectHome();
      panel = mainWindow.getPanel("welcomeView");
    }
    return panel;
  }
}
