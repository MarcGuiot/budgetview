package org.crossbowlabs.globs.sqlstreams.constraints;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.sqlstreams.constraints.impl.AccessorOperand;
import org.crossbowlabs.globs.sqlstreams.constraints.impl.ValueOperand;

public interface OperandVisitor {
  void visitValueOperand(ValueOperand value);

  void visitAccessorOperand(AccessorOperand accessorOperand);

  void visitFieldOperand(Field field);
}
