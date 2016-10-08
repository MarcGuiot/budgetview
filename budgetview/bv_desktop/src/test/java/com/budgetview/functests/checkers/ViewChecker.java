package com.budgetview.functests.checkers;

import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public abstract class ViewChecker extends GuiChecker {

  protected final Window mainWindow;
  protected final ViewSelectionChecker views;

  public ViewChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
    this.views = new ViewSelectionChecker(mainWindow);
  }

  protected UIComponent getMainComponent() {
    return mainWindow;
  }

  protected void checkPanelShown(String componentName) {
    assertThat(new Assertion() {
      public void check() {
        if (!mainWindow.getPanel(componentName).isVisible().isTrue()) {
          UISpecAssert.fail();
        }
      }
    }, 10000);
  }
}
