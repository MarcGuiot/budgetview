package org.designup.picsou.functests.checkers;

import org.uispec4j.*;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.interception.WindowHandler;

import junit.framework.Assert;

import javax.swing.*;
import java.awt.*;
import java.awt.Button;


public class RestoreSnapshotChecker extends GuiChecker {
  private Window dialog;

  public RestoreSnapshotChecker(Window dialog) {
    this.dialog = dialog;
  }

  public RestoreSnapshotChecker checkAvaillable(int count) {
    Component[] swingComponents = dialog.getSwingComponents(JButton.class, "date");
    Assert.assertEquals(count, swingComponents.length);
    return this;
  }

  public void restore(int position) {
    Component[] swingComponents = dialog.getSwingComponents(JButton.class, "date");
    org.uispec4j.Button button = new org.uispec4j.Button(((JButton)swingComponents[position]));
    WindowInterceptor.init(button.triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          MessageDialogChecker dialogChecker = new MessageDialogChecker(window);
          dialogChecker.checkMessageContains("successfully reloaded");
          return dialogChecker.triggerClose();
        }
      }).run();
    assertFalse(dialog.isVisible());
  }
}
