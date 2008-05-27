package org.crossbowlabs.globs.metamodel.fields;

import org.crossbowlabs.globs.metamodel.Field;

public interface DateField extends Field {
  void visit(FieldVisitor visitor) throws Exception;
}
