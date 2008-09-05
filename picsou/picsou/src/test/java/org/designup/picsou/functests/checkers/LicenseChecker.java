package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class LicenseChecker {
  private Window dialog;

  public LicenseChecker(Window dialog) {
    this.dialog = dialog;
  }

  static public void enterLicense(Window window, final String mail, final String code, final int monthCount) {
    WindowInterceptor.init(window.getMenuBar().getMenu("File")
      .getSubMenu("Register").triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getInputTextBox("mail").setText(mail);
          window.getInputTextBox("code").setText(code);
          window.getInputTextBox("monthCount").setText(Integer.toString(monthCount));
          return window.getButton("OK").triggerClick();
        }
      }).run();
  }

  static public void enterBadLicense(Window window, final String mail, final String code, final int monthCount) {
    WindowInterceptor.init(window.getMenuBar().getMenu("File")
      .getSubMenu("Register").triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getInputTextBox("mail").setText(mail);
          window.getInputTextBox("code").setText(code);
          window.getInputTextBox("monthCount").setText(Integer.toString(monthCount));
          window.getButton("ok").click();
          return window.getButton("cancel").triggerClick();
        }
      }).run();
  }

  public static LicenseChecker open(Window window) {
    return new LicenseChecker(WindowInterceptor.getModalDialog(window.getMenuBar().getMenu("File")
      .getSubMenu("Register").triggerClick()));
  }

  public LicenseChecker enterLicenseAndValidate(final String mail, final String code, final int monthCount) {
    enterLicense(mail, code, monthCount);
    dialog.getButton("ok").click();
    return this;
  }

  public LicenseChecker enterLicense(String mail, String code, int monthCount) {
    dialog.getInputTextBox("mail").setText(mail);
    dialog.getInputTextBox("code").setText(code);
    dialog.getInputTextBox("monthCount").setText(Integer.toString(monthCount));
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
