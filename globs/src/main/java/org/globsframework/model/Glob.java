package org.globsframework.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;

import java.io.Serializable;

public interface Glob extends FieldValues, Serializable {
  GlobType getType();

  Key getKey();

  FieldValues getTargetValues(Link link);

  boolean matches(FieldValues values);

  boolean matches(FieldValue... values);

  FieldValues getValues();

  boolean exists();

  Glob duplicate();
}
