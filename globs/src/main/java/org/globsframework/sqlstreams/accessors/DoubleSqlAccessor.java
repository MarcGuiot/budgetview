package org.globsframework.sqlstreams.accessors;

import org.globsframework.streams.accessors.DoubleAccessor;

public class DoubleSqlAccessor extends SqlAccessor implements DoubleAccessor {

  public Double getDouble() {
    return getSqlStream().getDouble(getIndex());
  }

  public double getValue() {
    return getDouble();
  }

  public Object getObjectValue() {
    return getDouble();
  }
}
