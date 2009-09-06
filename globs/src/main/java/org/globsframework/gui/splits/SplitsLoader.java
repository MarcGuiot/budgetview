package org.globsframework.gui.splits;

import java.awt.*;

public interface SplitsLoader {
  public void load(Component component, SplitHandler splitHandler);

  public static final SplitsLoader NULL = new SplitsLoader() {
    public void load(Component component, SplitHandler splitHandler) {
    }
  };
}
