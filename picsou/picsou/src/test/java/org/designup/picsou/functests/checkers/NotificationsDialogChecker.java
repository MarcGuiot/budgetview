package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import java.util.ArrayList;
import java.util.List;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class NotificationsDialogChecker extends ViewChecker {
  public static NotificationsDialogChecker open(Button button) {
    return new NotificationsDialogChecker(WindowInterceptor.getModalDialog(button.triggerClick()));
  }

  protected NotificationsDialogChecker(Window dialog) {
    super(dialog);
  }

  public NotificationsDialogChecker checkContent(String... expectedMessages) {
    StringBuffer actual = new StringBuffer();
    for (UIComponent panel : mainWindow.getUIComponents(Panel.class, "notificationPanel")) {
      actual.append(((Panel)panel).getTextBox("message").getText()).append("\n");
    }
    StringBuilder expected = new StringBuilder();
    for (String message : expectedMessages) {
      expected.append(message).append("\n");
    }
    Assert.assertEquals(expected.toString(), actual.toString());
    return this;
  }

  public NotificationsDialogChecker checkMessageCount(int count) {
    Assert.assertEquals(count, mainWindow.getUIComponents(Panel.class, "notificationPanel").length);
    return this;
  }

  public NotificationsDialogChecker checkMessage(int index, String message) {
    assertThat(getPanel(index).getTextBox("message").textContains(message));
    return this;
  }

  private Panel getPanel(int index) {
    UIComponent[] panels = mainWindow.getUIComponents(Panel.class, "notificationPanel");
    if (index > panels.length) {
      Assert.fail("Unexpected index " + index + " - actual content: " + panels.length);
    }
    return (Panel)panels[index];
  }

  public NotificationsDialogChecker runAction(int index) {
    getPanel(index).getButton("action").click();
    return this;
  }

  public NotificationsDialogChecker clearMessage(int index) {
    getPanel(index).getButton("delete").click();
    return this;
  }

  public void close() {
    mainWindow.getButton("OK").click();
  }
}
