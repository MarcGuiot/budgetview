package org.globsframework.metamodel.utils;

import org.globsframework.metamodel.index.MultiFieldIndexVisitor;
import org.globsframework.metamodel.index.MultiFieldUniqueIndex;

public class DefaultMultiFieldUniqueIndex extends AbstractMultiFieldIndex implements MultiFieldUniqueIndex {

  public DefaultMultiFieldUniqueIndex(String name) {
    super(name);
  }

  public void visitIndex(MultifieldVisitor visitor) {
    visitor.visitUnique(this);
  }

  public void visit(MultiFieldIndexVisitor multiFieldIndexVisitor) {
    multiFieldIndexVisitor.visitUnique(this);
  }

}
