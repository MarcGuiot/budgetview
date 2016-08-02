package com.budgetview.desktop.preferences;

public class PreferencesResult {
  private boolean close = true;
  private boolean exit = false;

  public void preventClose() {
    close = false;
  }

  public boolean shouldClose() {
    return close;
  }

  public void exitAfterClose() {
    exit = true;
  }

  public boolean shouldExit() {
    return exit;
  }
}
