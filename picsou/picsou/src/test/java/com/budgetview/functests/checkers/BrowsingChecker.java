package com.budgetview.functests.checkers;

import org.uispec4j.MenuItem;
import org.uispec4j.Trigger;
import org.uispec4j.Button;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.interception.WindowHandler;

public class BrowsingChecker {
  public static void checkDisplay(Button button, String url) {
    checkDisplay(button.triggerClick(), url);
  }

  public static void checkDisplay(MenuItem menu, String url) {
    checkDisplay(menu.triggerClick(), url);
  }

  public static void checkDisplay(Trigger trigger, String url) {
    Window window = WindowInterceptor.run(trigger);
    UISpecAssert.assertThat(window.getTextBox("url").textEquals(url));
    window.getButton("Close").click();
  }

  public static void checkDisplayedUrlContains(Trigger trigger, String urlPart) {
    Window window = WindowInterceptor.run(trigger);
    UISpecAssert.assertThat(window.getTextBox("url").textContains(urlPart));
    window.getButton("Close").click();
  }
}
