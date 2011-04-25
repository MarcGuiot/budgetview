package org.designup.picsou.functests.checkers;

import org.uispec4j.TextBox;
import org.uispec4j.Window;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.*;

public class NewVersionChecker extends GuiChecker {
  private Window mainWindow;

  public NewVersionChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  public NewVersionChecker checkNewVersionShown() {
    checkComponentVisible(mainWindow, JPanel.class, "newVersionPanel", true);
    checkComponentVisible(mainWindow, JLabel.class, "newVersionMessage", true);
    assertThat(mainWindow.getTextBox("newVersionMessage").textContains("new version"));
    return this;
  }

  public NewVersionChecker checkNoNewVersionShown() {
    checkComponentVisible(mainWindow, JPanel.class, "newVersionPanel", false);
    return this;
  }

  public void checkLink(String url) {
    BrowsingChecker.checkDisplay(mainWindow.getPanel("newVersionPanel").getButton("showChangeLog"), url);
  }

  public void hide() {
    mainWindow.getPanel("newVersionPanel").getButton("hide").click();
  }
}
