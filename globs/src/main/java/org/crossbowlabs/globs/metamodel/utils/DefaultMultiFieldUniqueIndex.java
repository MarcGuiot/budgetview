package org.crossbowlabs.globs.metamodel.utils;

import org.crossbowlabs.globs.metamodel.index.MultiFieldUniqueIndex;
import org.crossbowlabs.globs.metamodel.index.MultiFieldIndexVisitor;
import org.crossbowlabs.globs.metamodel.Field;

import java.util.List;

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
