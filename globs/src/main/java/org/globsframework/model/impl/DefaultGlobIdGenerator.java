package org.globsframework.model.impl;

import org.globsframework.model.utils.GlobIdGenerator;
import org.globsframework.metamodel.fields.IntegerField;

import java.util.Map;
import java.util.HashMap;

public class DefaultGlobIdGenerator implements GlobIdGenerator {
  private Map<IntegerField, Integer> fieldToCurrentId = new HashMap<IntegerField, Integer>();

  public DefaultGlobIdGenerator() {
  }

  public int getNextId(IntegerField keyField, int idCount) {
    Integer currentId = getNextCurrentId(keyField);
    try {
      return currentId;
    }
    finally {
      fieldToCurrentId.put(keyField, currentId + idCount);
    }
  }

  private Integer getNextCurrentId(IntegerField keyField) {
    Integer currentId = fieldToCurrentId.get(keyField);
    if (currentId == null) {
      return 100;
    }
    return currentId;
  }

  public void update(IntegerField field, Integer lastAllocatedId) {
    fieldToCurrentId.put(field, lastAllocatedId + 1);
  }
}
