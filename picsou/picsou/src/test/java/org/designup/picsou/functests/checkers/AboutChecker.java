package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;
import org.designup.picsou.gui.PicsouApplication;

public class AboutChecker extends GuiChecker {
  private Window dialog;

  public static AboutChecker open(Trigger trigger) {
    Window dialog = WindowInterceptor.getModalDialog(trigger);
    return new AboutChecker(dialog);
  }

  private AboutChecker(Window dialog) {
    this.dialog = dialog;
  }

  public void checkVersion() {
    UISpecAssert.assertThat(dialog.getTextBox("versionLabel").textContains(PicsouApplication.APPLICATION_VERSION));
  }

  public void checkConfigurationContains(String text) {
    UISpecAssert.assertThat(dialog.getTextBox("configurationArea").textContains(text));
  }

  public void close() {
    dialog.getButton("Close").click();
  }
}
