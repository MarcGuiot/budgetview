package org.crossbowlabs.splits;

import org.crossbowlabs.splits.layout.ComponentStretch;

import java.awt.*;

public interface Splitter {
  String getName();

  ComponentStretch getComponentStretch(boolean addMargin);

  Insets getMarginInsets();

  SplitProperties getProperties();
}
