package com.budgetview.functests.checkers;

import org.uispec4j.UIComponent;
import org.uispec4j.Window;

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
}
