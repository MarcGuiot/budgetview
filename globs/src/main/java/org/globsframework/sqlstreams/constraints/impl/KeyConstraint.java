package org.globsframework.sqlstreams.constraints.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Key;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.util.HashMap;
import java.util.Map;

public class KeyConstraint {
  private GlobType globType;
  private Map<Field, Object> values = new HashMap<Field, Object>();

  public KeyConstraint(GlobType globType) {
    this.globType = globType;
  }

  public void setValue(Key key) {
    for (FieldValue fieldValue : key.toArray()) {
      this.values.put(fieldValue.getField(), fieldValue.getValue());
    }
    if (key.getGlobType() != globType) {
      throw new UnexpectedApplicationState("Bad key received was " + key.getGlobType().getName() +
                                           " but " + globType.getName() + " was expected");
    }
  }

  public void setValue(Field field, Object value) {
    values.put(field, value);
  }

  public Object getValue(Field field) {
    return values.get(field);
  }

  public GlobType getGlobType() {
    return globType;
  }
}
