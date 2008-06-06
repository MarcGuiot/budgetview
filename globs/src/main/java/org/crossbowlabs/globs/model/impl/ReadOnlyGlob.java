package org.crossbowlabs.globs.model.impl;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.FieldValues;
import org.crossbowlabs.globs.model.Glob;

public class ReadOnlyGlob extends AbstractGlob {
  public ReadOnlyGlob(GlobType type, FieldValues values) {
    super(type, values);
  }

  private ReadOnlyGlob(GlobType type, Object[] values) {
    super(type, values);
  }

  public boolean exists() {
    return true;
  }

  void dispose() {
  }

  public Glob duplicate() {
    return new ReadOnlyGlob(type, duplicateValues());
  }

}
