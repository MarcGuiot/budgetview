package com.budgetview.functests.checkers.utils;

import junit.framework.Assert;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.uispec4j.Panel;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.finder.ComponentMatchers;

import javax.swing.*;
import javax.swing.text.JTextComponent;

public class MessageIsHiddenAssertion extends Assertion {

  private Panel panel;
  private String componentName;

  public MessageIsHiddenAssertion(Panel panel, String componentName) {
    this.panel = panel;
    this.componentName = componentName;
  }

  public void check() {
    final JComponent component = (JComponent) panel.findSwingComponent(ComponentMatchers.innerNameIdentity(componentName));
    if (component == null || !component.isVisible()) {
      return;
    }
    String text = getText(component);
    if (Strings.isNotEmpty(text)) {
      Assert.fail("Component '" + componentName + "' unexpectedly visible with message: " + text);
    }
  }

  private String getText(JComponent component) {
    if (JLabel.class.isInstance(component)) {
      return ((JLabel) component).getText();
    }
    else if (JTextComponent.class.isInstance(component)) {
      return ((JTextComponent) component).getText();
    }
    throw new InvalidParameter("Unexpected class '" + component.getClass().getSimpleName() + "' for component: " + componentName);
  }
}
