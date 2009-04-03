package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.Trigger;
import org.uispec4j.TextBox;
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
}
