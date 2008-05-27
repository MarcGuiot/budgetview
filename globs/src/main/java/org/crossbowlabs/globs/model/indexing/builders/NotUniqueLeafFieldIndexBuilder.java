package org.crossbowlabs.globs.model.indexing.builders;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.indexing.indices.NotUniqueLeafLevelIndex;
import org.crossbowlabs.globs.model.indexing.indices.UpdatableMultiFieldIndex;

public class NotUniqueLeafFieldIndexBuilder implements MultiFieldIndexBuilder {
  private Field field;

  public NotUniqueLeafFieldIndexBuilder(Field field) {
    this.field = field;
  }

  public UpdatableMultiFieldIndex create() {
    return new NotUniqueLeafLevelIndex(field);
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
