package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Window;

public class NavigationViewChecker extends DataChecker {
  private Window window;

  public NavigationViewChecker(Window window) {
    this.window = window;
  }

  public ImportChecker openImport() {
    return ImportChecker.open(getPanel().getButton("Import").triggerClick());
  }

  public HelpChecker openHelp() {
    return HelpChecker.open(getPanel().getButton("Help").triggerClick());
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
