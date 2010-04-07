package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Window;

public class NavigationViewChecker extends GuiChecker {
  private Window window;

  public NavigationViewChecker(Window window) {
    this.window = window;
  }

  public void gotoBudget() {
    getPanel().getButton("Budget").click();
  }

  public void gotoCategorization() {
    getPanel().getButton("Categorization").click();
  }

  private Panel getPanel() {
    return window.getPanel("navigationView");
  }
}
