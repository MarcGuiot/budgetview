package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;

import javax.swing.*;

public class AddOnsViewChecker extends ViewChecker {
  public AddOnsViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public void checkShown() {
    checkComponentVisible(mainWindow, JPanel.class, "addonsView", true);
  }

  public void checkHidden() {
    checkComponentVisible(mainWindow, JPanel.class, "addonsView", false);
  }

  public void checkButton(String url) {
    BrowsingChecker.checkDisplay(mainWindow.getPanel("addonsView").getButton("learmore"), url);
  }
}
