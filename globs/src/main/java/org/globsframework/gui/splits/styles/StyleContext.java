package org.globsframework.gui.splits.styles;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.impl.DefaultSplitProperties;
import org.globsframework.utils.exceptions.InvalidFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StyleContext {
  private List<Style> styles = new ArrayList<Style>();
  private Map<String, Style> styleByIds = new HashMap<String, Style>();

  public void createStyle(String id, Selector[] selectors, SplitProperties properties) {
    Style style = new Style(selectors, properties);
    styles.add(style);
    if (id != null) {
      if (styleByIds.put(id, style) != null){
        throw new InvalidFormat("Id '" + id + "' can not be use twice");
      }
    }
  }

  public void addAll(StyleContext other) {
    this.styles.addAll(other.styles);
    this.styleByIds.putAll(other.styleByIds);
  }

  public Style getStyle(String id){
    return styleByIds.get(id);
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
