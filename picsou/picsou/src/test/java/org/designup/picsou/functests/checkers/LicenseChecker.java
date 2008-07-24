package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class LicenseChecker {
  private Window window;

  public LicenseChecker(Window window) {
    this.window = window;
  }

  public void enterLicense(final String mail, final String code) {
    WindowInterceptor.init(window.getMenuBar().getMenu("File")
      .getSubMenu("Register").triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getInputTextBox("mail").setText(mail);
          window.getInputTextBox("code").setText(code);
          return window.getButton("OK").triggerClick();
        }
      }).run();
  }
}
