package org.globsframework.gui.splits.styles;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.impl.DefaultSplitProperties;

import java.util.ArrayList;
import java.util.List;

public class StyleContext {
  private List<Style> styles = new ArrayList<Style>();

  public void createStyle(Selector[] selectors, SplitProperties properties) {
    styles.add(new Style(selectors, properties));
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
