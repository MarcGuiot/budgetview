package com.budgetview.functests.checkers.utils;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowHandler;

public class ConfirmationHandler extends WindowHandler {

  private String title;
  private String message;
  private String button;

  public static ConfirmationHandler validate() {
    return new ConfirmationHandler(null, null, "OK");
  }

  public static ConfirmationHandler validate(String title, String message) {
    return new ConfirmationHandler(title, message, "OK");
  }

  public static ConfirmationHandler cancel() {
    return new ConfirmationHandler(null, null, "Cancel");
  }

  public static ConfirmationHandler cancel(String title, String message) {
    return new ConfirmationHandler(title, message, "Cancel");
  }

  public ConfirmationHandler(String title, String message, String button) {
    this.title = title;
    this.message = message;
    this.button = button;
  }

  public Trigger process(Window window) throws Exception {
    if (title != null) {
      UISpecAssert.assertThat(window.getTextBox("title").textEquals(title));
    }
    if (message != null) {
      UISpecAssert.assertThat(window.getTextBox("message").textContains(message));
    }
    return window.getButton(button).triggerClick();
  }
}
