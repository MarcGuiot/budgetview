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

  public HelpChecker clickLink(String hyperlinkText) {
    dialog.getTextBox("editor").clickOnHyperlink(hyperlinkText);
    return this;
  }

  public void close() {
    dialog.getButton("Close").click();
  }

  public void checkForwardEnabled(boolean enabled) {
    UISpecAssert.assertEquals(enabled, dialog.getButton("forward").isEnabled());
  }

  public void forward() {
    dialog.getButton("forward").click();
  }

  public void checkBackEnabled(boolean enabled) {
    UISpecAssert.assertEquals(enabled, dialog.getButton("back").isEnabled());
  }

  public void checkNavigation(boolean back, boolean forward) {
    checkBackEnabled(back);
    checkForwardEnabled(forward);
  }

  public void back() {
    dialog.getButton("back").click();
  }

  public void checkHomeEnabled(boolean enabled) {
    UISpecAssert.assertEquals(enabled, dialog.getButton("home").isEnabled());
  }

  public void home() {
    dialog.getButton("home").click();
  }
}
