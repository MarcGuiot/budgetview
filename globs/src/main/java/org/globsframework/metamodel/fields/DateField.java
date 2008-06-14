package org.globsframework.metamodel.fields;

import org.globsframework.metamodel.Field;

public interface DateField extends Field {
  void visit(FieldVisitor visitor) throws Exception;
}
