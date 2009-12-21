package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Button;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.interception.WindowHandler;

public class BrowsingChecker {
  public static void checkDisplay(Button button, String url) {
    Window window = WindowInterceptor.run(button.triggerClick());
    UISpecAssert.assertThat(window.getTextBox("url").textEquals(url));
  }
}
