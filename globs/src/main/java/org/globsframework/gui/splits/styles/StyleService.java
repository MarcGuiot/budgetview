package org.globsframework.gui.splits.styles;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.impl.DefaultSplitProperties;

import java.util.List;
import java.util.ArrayList;

public class StyleService {
  private List<Style> styles = new ArrayList<Style>();

  public void add(Style style) {
    styles.add(style);
  }

  public SplitProperties getProperties(SplitsPath path) {
    DefaultSplitProperties properties = new DefaultSplitProperties();
    for (Style style : styles) {
      if (style.matches(path)) {
        properties.add(style.getProperties());
      }
    }
    return properties;
  }
}
