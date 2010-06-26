package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.UISpec4J;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;

public class RenameChecker extends GuiChecker{
  private Window window;

  public RenameChecker(Window window) {
    this.window = window;
  }

  public void set(String currentPassword, String userName, String newPassword){
    window.getPasswordField("currentPassword").setPassword(currentPassword);
    window.getInputTextBox("newName").setText(userName);
    window.getPasswordField("newPassword").setPassword(newPassword);
    window.getPasswordField("confirmedPassword").setPassword(newPassword);
    window.getButton("ok").click();
    assertFalse(window.isVisible());
  }

  public void set(String userName, String newPassword){
    assertFalse(window.getPasswordField("currentPassword").isVisible());
    window.getInputTextBox("newName").setText(userName);
    window.getPasswordField("newPassword").setPassword(newPassword);
    window.getPasswordField("confirmedPassword").setPassword(newPassword);
    window.getButton("ok").click();
    assertFalse(window.isVisible());
  }
}
