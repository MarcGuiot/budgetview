package org.globsframework.gui.splits.color;

import java.awt.*;

public class ForegroundColorUpdater implements ColorUpdater {
  private final Component component;

  public ForegroundColorUpdater(Component component) {
    this.component = component;
  }

  public void updateColor(Color color) {
    component.setForeground(color);
  }
}
