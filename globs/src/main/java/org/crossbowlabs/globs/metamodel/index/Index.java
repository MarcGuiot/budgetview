package org.crossbowlabs.globs.metamodel.index;

import org.crossbowlabs.globs.metamodel.Field;

import java.io.Serializable;

public interface Index extends Serializable {
  String getName();

  Field getField();

  void visitIndex(IndexVisitor visitor);
}
