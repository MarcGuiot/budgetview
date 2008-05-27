package org.crossbowlabs.globs.streams.accessors.utils;

import org.crossbowlabs.globs.streams.accessors.LongAccessor;

public class ValueLongAccessor implements LongAccessor {
  private Long value;

  public ValueLongAccessor(Long value) {
    this.value = value;
  }

  public ValueLongAccessor() {
  }

  public Long getLong() {
    return value;
  }

  public long getValue() {
    return value;
  }

  public Object getObjectValue() {
    return value;
  }

  public void setValue(Long value) {
    this.value = value;
  }
}
