package org.designup.picsou.functests.checkers;

import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

public class LicenseExpirationChecker extends GuiChecker {
  private Window window;

  public LicenseExpirationChecker(Window window) {
    this.window = window;
  }

  public void close() {
    window.getButton("Cancel").click();
    UISpecAssert.assertFalse(window.isVisible());
  }

  public LicenseExpirationChecker checkMail(String mail) {
    window.getInputTextBox("mailAdress").textEquals(mail);
    return this;
  }

  public LicenseExpirationChecker sendKey() {
    window.getButton("sendMail").click();
    return this;
  }

}
