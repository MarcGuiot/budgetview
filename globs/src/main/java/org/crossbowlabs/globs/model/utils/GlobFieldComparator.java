package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.utils.Utils;

import java.util.Comparator;

public class GlobFieldComparator implements Comparator<Glob> {

  private Field field;

  public GlobFieldComparator(Field field) {
    this.field = field;
  }

  public int compare(Glob glob1, Glob glob2) {
    if ((glob1 == null) && (glob2 == null)) {
      return 0;
    }
    if (glob1 == null) {
      return -1;
    }
    if (glob2 == null) {
      return 1;
    }
    return Utils.compare((Comparable)glob1.getValue(field), glob2.getValue(field));
  }

  public String toString() {
    return getClass().getSimpleName() + "[" + field + "]";
  }
}
