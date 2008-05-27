package org.crossbowlabs.splits;

public interface SplitProperties {

  String[] getPropertyNames();

  String get(String propertyName);

  Integer getInt(String propertyName);

  Integer getInt(String propertyName, String parentPropertyName);

  Boolean getBoolean(String propertyName);

  String getString(String propertyName);

  String getString(String propertyName, String parentPropertyName);

  Double getDouble(String propertyName);

  Double getDouble(String propertyName, String parentPropertyName);
}
