package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.Trigger;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;

import org.uispec4j.interception.WindowInterceptor;

public class LicenseInfoChecker extends ViewChecker {
  private TextBox textBox;

  public LicenseInfoChecker(Window window) {
    super(window);
  }

  public LicenseExpirationChecker clickNewLicense() {
    Window dialog = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        getTextBox().clickOnHyperlink("Ask for a new code");
      }
    });
    return new LicenseExpirationChecker(dialog);
  }

  public LicenseInfoChecker checkMessage(String message) {
    assertThat(getTextBox().textContains(message));
    return this;
  }

  public LicenseInfoChecker clickLink(String text) {
    getTextBox().clickOnHyperlink(text);
    return this;
  }

  public void checkHidden() {
    if (getTextBox().isVisible().isTrue()) {
      Assert.fail("Should be hidden. Shown message: " + getTextBox().getText());
    }
  }

  public void checkVisible(String message) {
    TextBox box = getTextBox();
    assertTrue(box.textContains(message));
    assertTrue(box.isVisible());
  }

  private TextBox getTextBox() {
    if (textBox == null) {
      views.selectHome();
      textBox = mainWindow.getTextBox("licenseInfoMessage");
    }
    return textBox;
  }

}
