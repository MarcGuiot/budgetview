package org.globsframework.metamodel.links;

import org.globsframework.metamodel.Field;

public interface FieldMappingFunctor {
  void process(Field sourceField, Field targetField);
}
