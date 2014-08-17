package org.designup.picsou.functests.checkers.license;

import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.Trigger;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;

public class LicenseInfoChecker {
  private Window window;

  public LicenseInfoChecker(Window window) {
    this.window = window;
  }

  public LicenseExpirationChecker clickNewLicense() {
    Window dialog = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        getMessage().clickOnHyperlink("Ask for a new code");
      }
    });
    return new LicenseExpirationChecker(dialog);
  }

  public LicenseInfoChecker checkMessage(String message) {
    assertThat(getMessage().textContains(message));
    return this;
  }

  public LicenseInfoChecker clickLink(String text) {
    getMessage().clickOnHyperlink(text);
    return this;
  }

  private TextBox getMessage() {
    return window.getTextBox("licenseInfoMessage");
  }
}
