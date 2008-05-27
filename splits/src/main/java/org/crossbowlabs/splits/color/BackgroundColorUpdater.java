package org.crossbowlabs.splits.color;

import java.awt.*;

public class BackgroundColorUpdater implements ColorUpdater {
  private final Component component;

  public BackgroundColorUpdater(Component component) {
    this.component = component;
  }

  public void updateColor(Color color) {
    component.setBackground(color);
  }
}
