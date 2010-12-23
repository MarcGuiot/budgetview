package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

public abstract class ViewChecker extends GuiChecker {

  protected final Window mainWindow;
  protected final ViewSelectionChecker views;

  public ViewChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
    this.views = new ViewSelectionChecker(mainWindow);
  }

  public void checkVisible(boolean visible) {
    UIComponent component = getMainComponent();
    Assert.assertEquals(visible, component != null);
    if (component != null) {
      UISpecAssert.assertEquals(visible, component.isVisible());
    }
  }

  protected UIComponent getMainComponent() {
    return mainWindow;
  };
}
