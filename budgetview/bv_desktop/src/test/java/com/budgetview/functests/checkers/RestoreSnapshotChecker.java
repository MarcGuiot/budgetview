package com.budgetview.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.awt.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;


public class RestoreSnapshotChecker extends GuiChecker {
  private Window dialog;

  public RestoreSnapshotChecker(Window dialog) {
    this.dialog = dialog;
  }

  public RestoreSnapshotChecker checkAvailableSnapshots(int count) {
    Component[] swingComponents = dialog.getSwingComponents(JButton.class, "date");
    Assert.assertEquals(count, swingComponents.length);
    return this;
  }

  public void restore(int position) {
    restore(position, true);
  }

  public RestoreSnapshotChecker restoreWithCancel(int position) {
    restore(position, false);
    return this;
  }

  private void restore(int position, final boolean ok) {
    Component[] swingComponents = dialog.getSwingComponents(JButton.class, "date");
    org.uispec4j.Button button = new org.uispec4j.Button(((JButton) swingComponents[position]));
    WindowInterceptor interceptor = WindowInterceptor.init(button)
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          ConfirmationDialogChecker confirmationDialogChecker = new ConfirmationDialogChecker(window);
          confirmationDialogChecker.checkMessageContains("Do you realy want to restore");
          if (ok) {
            return confirmationDialogChecker.getOkTrigger();
          }
          else {
            return confirmationDialogChecker.getCancelTrigger();
          }
        }
      });
    if (ok) {
      interceptor
        .process(new WindowHandler() {
          public Trigger process(Window window) throws Exception {
            MessageDialogChecker dialogChecker = new MessageDialogChecker(window);
            dialogChecker.checkSuccessMessageContains("successfully reloaded");
            return dialogChecker.triggerClose();
          }
        }).run();
      assertFalse(dialog.isVisible());
    }
  }

  public void close() {
    dialog.getButton("close").click();
    assertFalse(dialog.isVisible());
  }
}
