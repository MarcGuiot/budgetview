package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

public abstract class ViewChecker extends GuiChecker {

  protected Window window;

  public ViewChecker(Window window) {
    this.window = window;
  }

  public void assertVisible(boolean visible) {
    UIComponent component = findMainComponent(window);
    Assert.assertEquals(visible, component != null);
    if (component != null) {
      UISpecAssert.assertEquals(visible, component.isVisible());
    }
  }

  protected abstract UIComponent findMainComponent(Window window);
}
