package org.globsframework.gui.splits.styles;

import org.globsframework.gui.splits.SplitProperties;

import java.util.Arrays;
import java.util.Iterator;

public class Style {
  private Selector[] filterSelectors;
  private SplitProperties properties;

  public Style(Selector[] selectors, SplitProperties properties) {
    this.filterSelectors = selectors;
    this.properties = properties;
  }

  public boolean matches(SplitsPath path) {
    Iterator<Selector> pathIterator = Arrays.asList(path.getSelectors()).iterator();
    for (Selector filterItem : filterSelectors) {
      boolean matched = false;
      while (pathIterator.hasNext()) {
        Selector pathItem = pathIterator.next();
        if (filterItem.matches(pathItem)) {
          matched = true;
          break;
        }
      }
      if (!matched && !pathIterator.hasNext()) {
        return false;
      }
    }
    return true;
  }

  public SplitProperties getProperties() {
    return properties;
  }
}
