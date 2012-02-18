package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.TextBox;
import org.uispec4j.Window;

import static org.uispec4j.assertion.UISpecAssert.*;

public class LicenseMessageChecker extends ViewChecker {

  private TextBox textBox;

  public LicenseMessageChecker(Window mainWindow) {
    super(mainWindow);
  }

  public void checkHidden() {
    if (getTextBox().isVisible().isTrue()) {
      Assert.fail("Should be hidden. Shown message: " + getTextBox().getText());
    }
  }

  public void checkVisible(String message) {
    TextBox box = getTextBox();
    assertTrue(box.isVisible());
    assertTrue(box.textContains(message));
  }

  private TextBox getTextBox() {
    if (textBox == null) {
      views.selectHome();
      textBox = mainWindow.getTextBox("licenseInfoMessage");
    }
    return textBox;
  }
}
