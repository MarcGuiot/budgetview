package org.crossbowlabs.globs.model.indexing.builders;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.indexing.indices.UpdatableMultiFieldIndex;
import org.crossbowlabs.globs.model.indexing.indices.UniqueLeafLevelIndex;

public class UniqueLeafFieldIndexBuilder implements MultiFieldIndexBuilder {
  private Field field;

  public UniqueLeafFieldIndexBuilder(Field field) {
    this.field = field;
  }

  public UpdatableMultiFieldIndex create() {
    return new UniqueLeafLevelIndex(field);
  }

  public MultiFieldIndexBuilder getSubBuilder() {
    return null;
  }

  public void setChild(MultiFieldIndexBuilder indexBuilder) {
  }

  public Field getField() {
    return field;
  }
}
