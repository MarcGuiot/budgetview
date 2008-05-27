package org.crossbowlabs.globs.model.impl;

import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.KeyBuilder;
import org.crossbowlabs.globs.model.utils.GlobIdGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultGlobIdGenerator implements GlobIdGenerator {
  private Map<IntegerField, Integer> fieldToCurrentId = new HashMap<IntegerField, Integer>();
  private GlobRepository repository;

  public DefaultGlobIdGenerator() {
  }

  public void setRepository(GlobRepository repository) {
    this.repository = repository;
  }

  public int getNextId(IntegerField keyField, int idCount) {
    Integer currentId = getNextCurrentId(keyField, idCount);
    try {
      return currentId;
    }
    finally {
      fieldToCurrentId.put(keyField, currentId + idCount);
    }
  }

  private Integer getNextCurrentId(IntegerField keyField, int idCount) {
    Integer currentId = fieldToCurrentId.get(keyField);
    if (currentId == null) {
      return maxId(keyField) + 1;
    }
    return getNextCurrentId(keyField, currentId, idCount);
  }

  private Integer getNextCurrentId(IntegerField keyField, Integer currentId, int idCount) {
    int next = currentId;

    if (repository == null) {
      return next;
    }

    for (; ;) {
      boolean ok = true;
      for (int i = 0; i < idCount; i++) {
        Glob alreadyExistingGlob = repository.find(KeyBuilder.init(keyField, next + i).get());
        if (alreadyExistingGlob != null) {
          next = next + i + 1;
          ok = false;
          break;
        }
      }

      if (ok) {
        return next;
      }
    }
  }

  private Integer maxId(IntegerField keyField) {
    if (repository == null) {
      return 0;
    }

    GlobList globs = repository.getAll(keyField.getGlobType());
    if (globs.isEmpty()) {
      return -1;
    }
    return Collections.max(globs.getValueSet(keyField));
  }
}
