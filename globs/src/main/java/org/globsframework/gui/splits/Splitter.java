package org.globsframework.gui.splits;


import org.globsframework.gui.splits.layout.ComponentConstraints;

import java.awt.*;

public interface Splitter {
  String getName();

  SplitComponent createComponentStretch(SplitsContext context, boolean addMargin);

  Insets getMarginInsets();

  SplitProperties getProperties();

  static class SplitComponent {
    final public ComponentConstraints componentConstraints;
    final public SplitsNode node;

    public SplitComponent(ComponentConstraints componentConstraints, SplitsNode node) {
      this.componentConstraints = componentConstraints;
      this.node = node;
    }
  }
}
