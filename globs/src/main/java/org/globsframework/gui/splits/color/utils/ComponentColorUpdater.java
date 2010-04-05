package org.globsframework.gui.splits.color.utils;

import org.globsframework.gui.splits.color.ColorUpdater;

import java.awt.*;
import java.lang.ref.WeakReference;

public abstract class ComponentColorUpdater extends ColorUpdater {

  private final WeakReference<Component> ref;

  public ComponentColorUpdater(String key, Component component) {
    super(key);
    ref = new WeakReference<Component>(component);
  }

  public void updateColor(Color color) {
    Component component = ref.get();
    if (component == null) {
      dispose();
      return;
    }
    doUpdateColor(component, color);
  }

  protected abstract void doUpdateColor(Component component, Color color);
}
