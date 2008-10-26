package org.designup.picsou.functests.checkers;

import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

public class LicenceExpirationChecker extends DataChecker {
  private Window window;


  public LicenceExpirationChecker(Trigger trigger) {
    window = WindowInterceptor.getModalDialog(trigger);
    TextBox box = window.getTextBox("expirationMessage");
    UISpecAssert.assertFalse(box.textIsEmpty());
  }

  public void close() {
    window.getButton("ok").click();
    UISpecAssert.assertFalse(window.isVisible());
  }
}
