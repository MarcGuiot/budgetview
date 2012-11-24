package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
import org.uispec4j.Button;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertThat;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;

public class NotificationsChecker extends ViewChecker {
  public NotificationsChecker(Window mainWindow) {
    super(mainWindow);
  }

  public NotificationsChecker checkVisible(int count) {
    Button button = mainWindow.getButton("notificationsFlag");
    assertTrue(button.isVisible());
    assertTrue(button.textEquals(Integer.toString(count)));
    return this;
  }

  public NotificationsChecker checkHidden() {
    checkComponentVisible(mainWindow, JButton.class, "notificationsFlag", false);
    return this;
  }

  public NotificationsDialogChecker openDialog() {
    Button button = mainWindow.getButton("notificationsFlag");
    assertTrue(button.isVisible());
    return NotificationsDialogChecker.open(button);
  }
}
