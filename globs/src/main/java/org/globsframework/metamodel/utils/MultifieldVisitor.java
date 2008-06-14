package org.globsframework.metamodel.utils;

import org.globsframework.metamodel.index.MultiFieldNotUniqueIndex;
import org.globsframework.metamodel.index.MultiFieldUniqueIndex;

public interface MultifieldVisitor {
  void visitUnique(MultiFieldUniqueIndex index);

  void visitNotUnique(MultiFieldNotUniqueIndex index);
}
