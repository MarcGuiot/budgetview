package org.crossbowlabs.globs.metamodel.index;

import org.crossbowlabs.globs.metamodel.Field;

public interface MultiFieldIndex {
  String getName();

  Field[] getFields();

  void visit(MultiFieldIndexVisitor multiFieldIndexVisitor);
}
