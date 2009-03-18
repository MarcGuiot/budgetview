package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class LicenseChecker {
  private Window dialog;

  public LicenseChecker(Window dialog) {
    this.dialog = dialog;
  }

  static public void enterLicense(Window window, final String mail, final String code) {
    enterLicense(window, new WindowHandler() {
      public Trigger process(Window window) throws Exception {
        window.getInputTextBox("mail").setText(mail);
        window.getInputTextBox("code").setText(code);
        return window.getButton("OK").triggerClick();
      }
    });
  }

  static public void enterBadLicense(Window window, final String mail, final String code, final String message) {
    enterLicense(window, new WindowHandler() {
      public Trigger process(Window window) throws Exception {
        window.getInputTextBox("mail").setText(mail);
        window.getInputTextBox("code").setText(code);
        window.getButton("ok").click();
        assertTrue(window.getTextBox("connectionMessage").textEquals(message));
        return window.getButton("cancel").triggerClick();
      }
    });
  }

  private static void enterLicense(Window window, WindowHandler windowHandler) {
    UISpecAssert.waitUntil(window.containsMenuBar(), 10000);
    WindowInterceptor.init(window.getMenuBar().getMenu("File").getSubMenu("Register").triggerClick())
      .process(windowHandler).run();
  }

  public static LicenseChecker open(Window window) {
    UISpecAssert.waitUntil(window.containsMenuBar(), 10000);
    return new LicenseChecker(WindowInterceptor.getModalDialog(window.getMenuBar().getMenu("File")
      .getSubMenu("Register").triggerClick()));
  }

  public LicenseChecker enterLicenseAndValidate(final String mail, final String code) {
    enterLicense(mail, code);
    dialog.getButton("ok").click();
    return this;
  }

  public LicenseChecker enterLicense(String mail, String code) {
    dialog.getInputTextBox("mail").setText(mail);
    dialog.getInputTextBox("code").setText(code);
    return this;
  }

  public void checkConnectionNotAvailable() {
    assertFalse(dialog.getInputTextBox("mail").isEnabled());
    assertFalse(dialog.getInputTextBox("code").isEnabled());
    assertTrue(dialog.getTextBox("connectionMessage").textEquals("You must be connected to Internet to register"));
  }

  public void checkConnectionIsAvailable() {
    assertTrue(dialog.getInputTextBox("mail").isEnabled());
    assertTrue(dialog.getInputTextBox("code").isEnabled());
    assertFalse(dialog.getTextBox("connectionMessage").isVisible());
  }

  public LicenseChecker checkErrorMessage(String message) {
    assertFalse(dialog.getProgressBar().isVisible());
    assertTrue(dialog.getTextBox("connectionMessage").textEquals(message));
    return this;
  }

  public void cancel() {
    dialog.getButton("cancel").click();
    assertFalse(dialog.isVisible());
  }

  public LicenseChecker validate() {
    dialog.getButton("ok").click();
    return this;
  }
}
