package org.globsframework.gui.splits.color.utils;

import java.awt.*;

public class BackgroundColorUpdater extends ComponentColorUpdater {
  public BackgroundColorUpdater(String key, Component component) {
    super(key, component);
  }

  protected void doUpdateColor(Component component, Color color) {
    component.setBackground(color);
  }
}
