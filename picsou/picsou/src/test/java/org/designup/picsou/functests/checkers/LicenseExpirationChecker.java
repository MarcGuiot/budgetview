package org.designup.picsou.functests.checkers;

import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

import java.io.Closeable;

public class LicenseExpirationChecker extends GuiChecker {
  private Window window;

  public LicenseExpirationChecker(Window window) {
    this.window = window;
  }

  public LicenseExpirationChecker(Trigger trigger) {
    window = WindowInterceptor.getModalDialog(trigger);
    TextBox box = window.getTextBox("expirationMessage");
    UISpecAssert.assertFalse(box.textIsEmpty());
  }

  public void close() {
    window.getButton("ok").click();
    UISpecAssert.assertFalse(window.isVisible());
  }

  public LicenseExpirationChecker checkMail(String mail){
    window.getInputTextBox("mailAdress").textEquals(mail);
    return this;
  }

  public LicenseExpirationChecker sendKey() {
    window.getButton("sendMail").click();
    return this;
  }

  public LicenseExpirationChecker checkMessageMailSent() {
    window.getTextBox("mailResponse").textEquals("A mail was send to you");
    return this;
  }
}
