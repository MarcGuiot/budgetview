package org.crossbowlabs.globs.streams.accessors.utils;

import org.crossbowlabs.globs.streams.accessors.DoubleAccessor;

public class ValueDoubleAccessor implements DoubleAccessor {
  private Double value;

  public ValueDoubleAccessor(Double value) {
    this.value = value;
  }

  public void setValue(Double value) {
    this.value = value;
  }

  public Double getDouble() {
    return value;
  }

  public double getValue() {
    return value;
  }

  public Object getObjectValue() {
    return value;
  }
}
