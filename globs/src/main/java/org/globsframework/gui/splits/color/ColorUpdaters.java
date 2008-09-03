package org.globsframework.gui.splits.color;

import java.awt.*;
import java.lang.ref.WeakReference;

public class ColorUpdaters {

  public static ColorUpdater background(final Component component) {
    final WeakReference<Component> ref = new WeakReference<Component>(component);
    return new ColorUpdater() {
      public void updateColor(Color color) {
        Component c = ref.get();
        if (c != null) {
          c.setBackground(color);
        }
      }
    };
  }

  public static ColorUpdater foreground(final Component component) {
    final WeakReference<Component> ref = new WeakReference<Component>(component);
    return new ColorUpdater() {
      public void updateColor(Color color) {
        Component c = ref.get();
        if (c != null) {
          c.setForeground(color);
        }
      }
    };
  }

}
