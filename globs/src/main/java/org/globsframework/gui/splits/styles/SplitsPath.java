package org.globsframework.gui.splits.styles;

import org.globsframework.utils.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplitsPath {

  private Selector[] selectors;

  public SplitsPath(Selector[] selectors) {
    this.selectors = selectors;
  }

  public SplitsPath(SplitsPath parent, String typeName, String componentName, String styleClassName) {
    this(createItems(parent, typeName, componentName, styleClassName));
  }

  private static Selector[] createItems(SplitsPath parent, String typeName, String componentName, String className) {
    List<Selector> itemList = new ArrayList<Selector>();
    if (parent != null) {
      itemList.addAll(Arrays.asList(parent.selectors));
    }
    itemList.add(new Selector(typeName, componentName, className));
    return itemList.toArray(new Selector[itemList.size()]);
  }

  public Selector[] getSelectors() {
    return selectors;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Selector selector : selectors) {
      if (builder.length() > 0) {
        builder.append(' ');
      }
      builder.append(selector);
    }
    return builder.toString();
  }
}
