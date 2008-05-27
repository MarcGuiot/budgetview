package org.crossbowlabs.splits.impl;

import org.crossbowlabs.splits.SplitProperties;

public abstract class AbstractSplitProperties implements SplitProperties {

  private SplitProperties parent;

  public AbstractSplitProperties() {
  }

  public AbstractSplitProperties(SplitProperties parent) {
    this.parent = parent;
  }

  public abstract String get(String propertyName);

  public String getString(String propertyName, String parentPropertyName) {
    String result = getString(propertyName);
    if ((result == null) && (parent != null)) {
      return parent.getString(parentPropertyName);
    }
    return result;
  }

  public Integer getInt(String propertyName) {
    String value = get(propertyName);
    if (value == null) {
      return null;
    }
    return Integer.valueOf(value);
  }

  public Integer getInt(String propertyName, String parentPropertyName) {
    Integer result = getInt(propertyName);
    if ((result == null) && (parent != null)) {
      return parent.getInt(parentPropertyName);
    }
    return result;
  }

  public Double getDouble(String propertyName) {
    String value = get(propertyName);
    if (value == null) {
      return null;
    }
    return Double.valueOf(value);
  }

  public Double getDouble(String propertyName, String parentPropertyName) {
    Double result = getDouble(propertyName);
    if ((result == null) && (parent != null)) {
      return parent.getDouble(parentPropertyName);
    }
    return result;
  }

  public Boolean getBoolean(String propertyName) {
    String value = get(propertyName);
    if (value == null) {
      return null;
    }
    return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
  }

  public String getString(String propertyName) {
    return get(propertyName);
  }
}
