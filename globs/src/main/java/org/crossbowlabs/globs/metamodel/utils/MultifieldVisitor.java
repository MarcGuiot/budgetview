package org.crossbowlabs.globs.metamodel.utils;

import org.crossbowlabs.globs.metamodel.index.MultiFieldNotUniqueIndex;
import org.crossbowlabs.globs.metamodel.index.MultiFieldUniqueIndex;

public interface MultifieldVisitor {
  void visitUnique(MultiFieldUniqueIndex index);

  void visitNotUnique(MultiFieldNotUniqueIndex index);
}
