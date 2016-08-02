package com.budgetview.desktop.components.utils;

import java.awt.*;
import java.util.Arrays;

public class CustomFocusTraversalPolicy extends FocusTraversalPolicy {
  private java.util.List<Component> order;

  public CustomFocusTraversalPolicy(Component... order) {
    this.order = Arrays.asList(order);
  }

  public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
    int idx = (order.indexOf(aComponent) + 1) % order.size();
    Component component = order.get(idx);
    if (component.isVisible()) {
      return component;
    }
    else {
      return getComponentAfter(focusCycleRoot, component);
    }
  }

  public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
    int idx = order.indexOf(aComponent) - 1;
    if (idx < 0) {
      idx = order.size() - 1;
    }
    Component component = order.get(idx);
    if (component.isVisible()) {
      return component;
    }
    else {
      return getComponentBefore(focusCycleRoot, component);
    }
  }

  public Component getDefaultComponent(Container focusCycleRoot) {
    return order.get(0);
  }

  public Component getLastComponent(Container focusCycleRoot) {
    return order.get(order.size() - 1);
  }

  public Component getFirstComponent(Container focusCycleRoot) {
    return order.get(0);
  }
}
