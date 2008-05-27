package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;

public interface GlobIdGenerator {
  int getNextId(IntegerField keyField, int idCount);

  GlobIdGenerator NONE = new GlobIdGenerator() {
    public int getNextId(IntegerField keyField, int idCount) {
      throw new UnexpectedApplicationState("No ID generator registered for type: " + keyField.getName());
    }
  };

}
