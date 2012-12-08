package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.utils.Lang;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class NotificationsDialogChecker extends ViewChecker {
  public static NotificationsDialogChecker open(Button button) {
    return new NotificationsDialogChecker(WindowInterceptor.getModalDialog(button.triggerClick()));
  }

  protected NotificationsDialogChecker(Window dialog) {
    super(dialog);
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

  public void validate() {
    mainWindow.getButton("OK").click();
  }
}
