package org.crossbowlabs.globs.model.impl;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.FieldValues;

public class DefaultGlob extends AbstractMutableGlob {

  public DefaultGlob(GlobType type) {
    super(type);
  }

  public DefaultGlob(GlobType type, Object[] values) {
    super(type, values);
  }

  public DefaultGlob(GlobType type, FieldValues values) {
    super(type);
    setValues(values);
  }
}
