package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;

public class GlobFieldMatcher implements GlobMatcher {
  private Field field;
  private Object value;

  public GlobFieldMatcher(Field field, Object value) {
    this.field = field;
    this.value = value;
  }

  public boolean matches(Glob item, GlobRepository repository) {
    if (item == null) {
      return false;
    }
    Object actualValue = item.getValue(field);
    if (actualValue == null) {
      return value == actualValue;
    }
    return actualValue.equals(value);
  }
}
