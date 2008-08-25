package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class LicenseChecker {
  private Window window;

  public LicenseChecker(Window window) {
    this.window = window;
  }

  public void enterLicense(final String mail, final String code, final int monthCount) {
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

  public void enterBadLicense(final String mail, final String code, final int monthCount) {
    WindowInterceptor.init(window.getMenuBar().getMenu("File")
      .getSubMenu("Register").triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getInputTextBox("mail").setText(mail);
          window.getInputTextBox("code").setText(code);
          window.getInputTextBox("monthCount").setText(Integer.toString(monthCount));
          window.getButton("OK").click();
          return window.getButton("Cancel").triggerClick();
        }
      }).run();
  }


  public static void checkConnectionNotAvailable(Window window) {
    assertFalse(window.getInputTextBox("mail").isEnabled());
    assertFalse(window.getInputTextBox("code").isEnabled());
    assertTrue(window.getTextBox("connectionMessage").textEquals("You must be connected to Internet to register"));
  }

  public static void checkConnectionIsAvailable(Window window) {
    assertTrue(window.getInputTextBox("mail").isEnabled());
    assertTrue(window.getInputTextBox("code").isEnabled());
    assertFalse(window.getTextBox("connectionMessage").isVisible());
  }
}
