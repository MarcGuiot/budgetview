package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;

public class HelpChecker extends DataChecker {
  private Window dialog;

  public static HelpChecker open(Trigger trigger) {
    return new HelpChecker(trigger);
  }

  public HelpChecker(Trigger trigger) {
    dialog = WindowInterceptor.run(trigger);
  }

  public HelpChecker checkTitle(String title) {
    UISpecAssert.assertThat(dialog.getTextBox("title").textEquals(title));
    return this;
  }

  public HelpChecker checkContains(String text) {
    UISpecAssert.assertThat(dialog.getTextBox("editor").textContains(text));
    return this;
  }

  public void close() {
    dialog.getButton("Close").click();
  }
}
