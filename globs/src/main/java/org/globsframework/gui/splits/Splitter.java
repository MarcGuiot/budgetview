package org.globsframework.gui.splits;


import org.globsframework.gui.splits.layout.ComponentStretch;

import java.awt.*;

public interface Splitter {
  String getName();

  SplitComponent createComponentStretch(SplitsContext context, boolean addMargin);

  Insets getMarginInsets();

  SplitProperties getProperties();

  static class SplitComponent {
    final public ComponentStretch componentStretch;
    final public SplitHandler handler;

    public SplitComponent(ComponentStretch componentStretch, SplitHandler handler) {
      this.componentStretch = componentStretch;
      this.handler = handler;
    }
  }
}
