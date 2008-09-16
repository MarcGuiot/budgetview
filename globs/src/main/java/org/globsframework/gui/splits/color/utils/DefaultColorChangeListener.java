package org.globsframework.gui.splits.color.utils;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;

import java.awt.*;

public abstract class DefaultColorChangeListener implements ColorChangeListener {
  private String key;

  protected DefaultColorChangeListener(String key) {
    this.key = key;
  }

  public void colorsChanged(ColorLocator colorLocator) {
    updateColor(colorLocator.get(key));
  }

  protected abstract void updateColor(Color color);
}
