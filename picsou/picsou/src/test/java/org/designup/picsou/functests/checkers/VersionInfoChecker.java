package org.designup.picsou.functests.checkers;

import org.uispec4j.TextBox;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.*;

public class VersionInfoChecker extends GuiChecker {
  private Window mainWindow;

  public VersionInfoChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  public VersionInfoChecker checkNewVersion() {
    assertThat(mainWindow.containsUIComponent(TextBox.class, "newVersionMessage"));
    assertThat(mainWindow.getTextBox("newVersionMessage").textContains("new version"));
    return this;
  }

  public VersionInfoChecker checkNoNewVersion() {
    assertFalse(mainWindow.getTextBox("newVersionMessage").isVisible());
    return this;
  }
}
