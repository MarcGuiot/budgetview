package org.globsframework.model.repository;

import org.globsframework.model.repository.GlobIdGenerator;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.GlobType;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

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

  public void reset(Collection<GlobType> collection){
    for (Iterator<IntegerField> it = fieldToCurrentId.keySet().iterator(); it.hasNext();) {
      IntegerField field = it.next();
      if (collection.contains(field.getGlobType())) {
        it.remove();
      }
    }
  }
}
