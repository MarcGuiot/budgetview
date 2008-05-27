package org.crossbowlabs.globs.model.utils;

import java.util.Comparator;
import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.Glob;

public class ReverseGlobFieldComparator implements Comparator<Glob> {

  private GlobFieldComparator inner;

  public ReverseGlobFieldComparator(Field field) {
    inner = new GlobFieldComparator(field);
  }

  public int compare(Glob o1, Glob o2) {
    return inner.compare(o1, o2) * -1;
  }
}
