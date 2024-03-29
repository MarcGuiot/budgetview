package com.budgetview.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.Window;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertTrue;

public class NotificationsChecker extends ViewChecker {
  public NotificationsChecker(Window mainWindow) {
    super(mainWindow);
  }

  public NotificationsChecker checkVisible(int count) {
    checkComponentVisible(mainWindow, JButton.class, "notificationsFlag", true);
    assertTrue(mainWindow.getButton("notificationsFlag").textEquals(Integer.toString(count)));
    return this;
  }

  public NotificationsChecker checkHidden() {
    checkComponentVisible(mainWindow, JButton.class, "notificationsFlag", false);
    return this;
  }

  public void checkContent(final String... content) {
    if (content.length == 0) {
      checkHidden();
      return;
    }
    final Button button = mainWindow.getButton("notificationsFlag");
    assertTrue("Notification button is not visible", button.isVisible());
    openDialog()
      .checkContent(content)
      .close();
    assertTrue(button.textEquals(Integer.toString(content.length)));
  }

  public NotificationsDialogChecker openDialog() {
    checkComponentVisible(mainWindow, JButton.class, "notificationsFlag", true);
    return NotificationsDialogChecker.open(mainWindow.getButton("notificationsFlag"));
  }
}
