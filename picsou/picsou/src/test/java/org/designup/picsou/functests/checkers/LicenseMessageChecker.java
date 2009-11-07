package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.Trigger;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;

public class LicenseMessageChecker {
  private Window window;

  public LicenseMessageChecker(Window window) {
    this.window = window;
  }

  public LicenseExpirationChecker clickNewLicense() {
    Window dialog = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        window.getTextBox("licenseMessage").clickOnHyperlink("Ask for a new code");
      }
    });
    return new LicenseExpirationChecker(dialog);
  }

  public LicenseMessageChecker checkMessage(String message) {
    assertThat(window.getTextBox("licenseMessage").textContains(message));
    return this;
  }

  public LicenseMessageChecker clickLink(String text) {
    window.getTextBox("licenseMessage").clickOnHyperlink(text);
    return this;
  }
}
