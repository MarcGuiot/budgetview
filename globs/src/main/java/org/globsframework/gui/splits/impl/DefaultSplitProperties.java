package org.globsframework.gui.splits.impl;

import org.globsframework.gui.splits.SplitProperties;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DefaultSplitProperties extends AbstractSplitProperties {

  private Map<String, String> properties = new HashMap<String, String>();

  public DefaultSplitProperties() {
  }

  public DefaultSplitProperties(SplitProperties parent) {
    super(parent);
  }

  public String[] getPropertyNames() {
    Collection<String> keys = properties.keySet();
    return keys.toArray(new String[keys.size()]);
  }

  public boolean contains(String propertyName) {
    return properties.containsKey(propertyName);
  }

  public String get(String propertyName) {
    return properties.get(propertyName);
  }

  public void put(String key, String value) {
    properties.put(key, value);
  }

  public String toString() {
    return properties.toString();
  }

  public void add(SplitProperties other) {
    for (String name : other.getPropertyNames()) {
      put(name, other.get(name));
    }
  }
}
