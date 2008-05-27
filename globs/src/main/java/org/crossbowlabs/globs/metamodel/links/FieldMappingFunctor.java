package org.crossbowlabs.globs.metamodel.links;

import org.crossbowlabs.globs.metamodel.Field;

public interface FieldMappingFunctor {
  void process(Field sourceField, Field targetField);
}
