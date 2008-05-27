package org.crossbowlabs.globs.streams.accessors.utils;

import org.crossbowlabs.globs.streams.accessors.StringAccessor;

public class ValueStringAccessor implements StringAccessor {
  private String value;

  public ValueStringAccessor(String value) {
    this.value = value;
  }

  public ValueStringAccessor() {
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getString() {
    return value;
  }

  public Object getObjectValue() {
    return value;
  }
}
