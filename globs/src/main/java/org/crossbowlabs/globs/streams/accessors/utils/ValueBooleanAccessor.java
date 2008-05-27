package org.crossbowlabs.globs.streams.accessors.utils;

import org.crossbowlabs.globs.streams.accessors.BooleanAccessor;

public class ValueBooleanAccessor implements BooleanAccessor {
  private Boolean value;

  public ValueBooleanAccessor(Boolean value) {
    this.value = value;
  }

  public void setValue(Boolean value) {
    this.value = value;
  }

  public Object getObjectValue() {
    return value;
  }

  public Boolean getBoolean() {
    return value;
  }

  public boolean getValue() {
    return value;
  }
}
