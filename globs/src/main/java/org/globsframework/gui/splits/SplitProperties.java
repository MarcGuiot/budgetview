package org.globsframework.gui.splits;

public interface SplitProperties {

  String[] getPropertyNames();

  boolean contains(String layout);

  String get(String propertyName);

  Integer getInt(String propertyName);

  Integer getInt(String propertyName, String parentPropertyName);

  Boolean getBoolean(String propertyName);

  String getString(String propertyName);

  String getString(String propertyName, SplitsContext context);

  String getString(String propertyName, String parentPropertyName);

  Double getDouble(String propertyName);

  Double getDouble(String propertyName, String parentPropertyName);

}
