package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;

public class InfoChecker extends GuiChecker {
  private Window mainWindow;

  public InfoChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  public InfoChecker checkNewVersion() {
    UISpecAssert.assertThat(mainWindow.containsUIComponent(TextBox.class, "versionInfo"));
    UISpecAssert.assertThat(mainWindow.getTextBox("versionInfo").textContains("new version"));
    return this;
  }

  public InfoChecker checkNoNewVersion() {
    checkComponentVisible(mainWindow, JPanel.class, "informationPanel", false);
    return this;
  }
}
