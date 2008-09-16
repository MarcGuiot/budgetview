package org.globsframework.gui.splits.color;

import org.globsframework.gui.splits.utils.Disposable;

import java.awt.*;

public abstract class ColorUpdater implements ColorChangeListener, Disposable {
  private String key;
  private ColorService colorService;

  protected ColorUpdater(String key) {
    this.key = key;
  }

  public void install(ColorService colorService) {
    this.colorService = colorService;
    this.colorService.addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    updateColor(colorLocator.get(key));
  }

  public abstract void updateColor(Color color);

  public void dispose() {
    if (colorService != null) {
      colorService.removeListener(this);
    }
  }
}
