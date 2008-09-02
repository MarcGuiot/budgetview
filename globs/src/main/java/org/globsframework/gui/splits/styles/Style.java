package org.globsframework.gui.splits.styles;

import org.globsframework.gui.splits.SplitProperties;

public class Style {
  private Selector[] filterSelectors;
  private SplitProperties properties;

  public Style(Selector[] selectors, SplitProperties properties) {
    this.filterSelectors = selectors;
    this.properties = properties;
  }

  public boolean matches(SplitsPath path) {
    final Selector[] pathIterator = path.getSelectors();
    int i = 0;
    for (Selector filterItem : filterSelectors) {
      boolean matched = false;
      while (i < pathIterator.length) {
        Selector pathItem = pathIterator[i];
        if (filterItem.matches(pathItem)) {
          matched = true;
          break;
        }
        i++;
      }
      if (!matched && i == pathIterator.length) {
        return false;
      }
    }
    Selector lastFilterItem = filterSelectors[filterSelectors.length - 1];
    Selector lastPathItem = path.getSelectors()[path.getSelectors().length - 1];
    return lastFilterItem.matches(lastPathItem);
  }

  public SplitProperties getProperties() {
    return properties;
  }
}
