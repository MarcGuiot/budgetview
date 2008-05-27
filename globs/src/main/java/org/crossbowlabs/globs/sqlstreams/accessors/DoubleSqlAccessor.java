package org.crossbowlabs.globs.sqlstreams.accessors;

import org.crossbowlabs.globs.streams.accessors.DoubleAccessor;

public class DoubleSqlAccessor extends SqlAccessor implements DoubleAccessor {

  public Double getDouble() {
    return getSqlMoStream().getDouble(getIndex());
  }

  public double getValue() {
    return getDouble();
  }

  public Object getObjectValue() {
    return getDouble();
  }
}
