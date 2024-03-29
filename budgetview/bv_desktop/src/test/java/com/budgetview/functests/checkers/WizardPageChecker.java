package com.budgetview.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Window;

public class WizardPageChecker<T> extends GuiChecker {

  private Window window;

  protected WizardPageChecker(Window window) {
    this.window = window;
  }

  public WizardPageChecker(WizardPageChecker page) {
    this.window = page.window;
  }

  public WizardPageChecker gotoPage(String title) {
    getControlPanel().getComboBox("combo").select(title);
    return this;
  }

  private Panel getControlPanel() {
    return window.getPanel("wizardControls");
  }

  public Panel getContent() {
    return window.getPanel("content");
  }

  public void close() {
    window.getButton("Close").click();
  }
}
