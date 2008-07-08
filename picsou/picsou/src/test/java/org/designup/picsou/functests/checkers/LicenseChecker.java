package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;

public class LicenseChecker {
  private Window window;

  public LicenseChecker(Window window) {
    this.window = window;
  }

  public void enterLicense(String license) {
    window.getMenuBar().getMenu("File").getSubMenu("Register").click();
  }
}
