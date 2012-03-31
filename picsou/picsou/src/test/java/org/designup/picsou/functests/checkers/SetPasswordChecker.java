package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.UISpec4J;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.*;

public class SetPasswordChecker extends GuiChecker{
  private Window window;

  public SetPasswordChecker(Window window) {
    this.window = window;
  }

  public SetPasswordChecker setPassword(String userName, String newPassword){
    assertFalse(window.getPasswordField("currentPassword").isVisible());
    window.getInputTextBox("newName").setText(userName);
    window.getPasswordField("newPassword").setPassword(newPassword);
    window.getPasswordField("confirmedPassword").setPassword(newPassword);
    return this;
  }

  public SetPasswordChecker changePassword(String currentPassword, String userName, String newPassword){
    window.getPasswordField("currentPassword").setPassword(currentPassword);
    window.getInputTextBox("newName").setText(userName);
    window.getPasswordField("newPassword").setPassword(newPassword);
    window.getPasswordField("confirmedPassword").setPassword(newPassword);
    return this;
  }

  public void validate(String title, String message) {
    MessageDialogChecker.open(window.getButton("ok").triggerClick())
      .checkTitle(title)
      .checkMessageContains(message)
      .close();
    assertFalse(window.isVisible());
  }

  public void validate() {
    MessageDialogChecker.open(window.getButton("ok").triggerClick()).close();
    assertFalse(window.isVisible());    
  }
}
