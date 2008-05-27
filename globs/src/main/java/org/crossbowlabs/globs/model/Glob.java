package org.crossbowlabs.globs.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.Link;

import java.io.Serializable;

public interface Glob extends FieldValues, Serializable {
  GlobType getType();

  Key getKey();

  FieldValues getTargetValues(Link link);

  boolean matches(FieldValues values);

  boolean matches(FieldValue... values);

  FieldValues getValues(boolean includeKeyFields);

  boolean exists();
}
