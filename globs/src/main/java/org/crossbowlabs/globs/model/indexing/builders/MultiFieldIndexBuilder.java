package org.crossbowlabs.globs.model.indexing.builders;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.indexing.indices.UpdatableMultiFieldIndex;

public interface MultiFieldIndexBuilder {
  UpdatableMultiFieldIndex create();

  MultiFieldIndexBuilder getSubBuilder();

  void setChild(MultiFieldIndexBuilder indexBuilder);

  Field getField();
}
