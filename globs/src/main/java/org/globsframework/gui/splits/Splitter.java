package org.globsframework.gui.splits;


import org.globsframework.gui.splits.layout.ComponentStretch;

import java.awt.*;

public interface Splitter {
  String getName();

  ComponentStretch createComponentStretch(SplitsContext context, boolean addMargin);

  Insets getMarginInsets();

  SplitProperties getProperties();
}
