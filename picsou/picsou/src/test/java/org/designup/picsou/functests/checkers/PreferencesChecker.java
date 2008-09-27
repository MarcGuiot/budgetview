package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

public class PreferencesChecker extends DataChecker {
  private Window window;

  public PreferencesChecker(Window window) {
    this.window = window;
  }

  public PreferencesChecker changeFutureMonth(int month) {
    window.getComboBox("futureMonth").select(Integer.toString(month));
    return this;
  }

  public void validate() {
    window.getButton("ok").click();
    UISpecAssert.assertFalse(window.isVisible());
  }

  public void cancel() {
    window.getButton("cancel").click();
    UISpecAssert.assertFalse(window.isVisible());
  }
}
