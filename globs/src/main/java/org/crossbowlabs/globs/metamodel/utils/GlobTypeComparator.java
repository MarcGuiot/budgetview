package org.crossbowlabs.globs.metamodel.utils;

import org.crossbowlabs.globs.metamodel.GlobType;

import java.util.Comparator;

public class GlobTypeComparator {
  public static final Comparator<GlobType> INSTANCE = new Comparator<GlobType>() {
    public int compare(GlobType o1, GlobType o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };
}
