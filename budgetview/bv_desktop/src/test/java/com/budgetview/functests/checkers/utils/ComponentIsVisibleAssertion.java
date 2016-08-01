package com.budgetview.functests.checkers.utils;

import junit.framework.Assert;
import org.uispec4j.Panel;
import org.uispec4j.assertion.Assertion;

import javax.swing.*;

public class ComponentIsVisibleAssertion<T extends JComponent> extends Assertion {
  private Panel panel;
  private Class<T> swingComponentClass;
  private String componentName;
  private boolean visible;

  public ComponentIsVisibleAssertion(Panel panel,
                                     Class<T> swingComponentClass,
                                     String componentName,
                                     boolean visible) {
    this.panel = panel;
    this.swingComponentClass = swingComponentClass;
    this.componentName = componentName;
    this.visible = visible;
  }

  public void check() {
    final JComponent component = panel.findSwingComponent(swingComponentClass, componentName);
    Assert.assertEquals(visible, component != null && component.isVisible());
  }
}
