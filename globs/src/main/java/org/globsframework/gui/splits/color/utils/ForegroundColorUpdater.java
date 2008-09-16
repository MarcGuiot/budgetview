package org.globsframework.gui.splits.color.utils;

import java.awt.*;

public class ForegroundColorUpdater extends ComponentColorUpdater {
  public ForegroundColorUpdater(String key, Component component) {
    super(key, component);
  }

  protected void doUpdateColor(Component component, Color color) {
    component.setForeground(color);
  }
}