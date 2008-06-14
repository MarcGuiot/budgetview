package org.globsframework.utils;

import java.util.Comparator;

public class DefaultComparator<T extends Comparable> implements Comparator<T> {
  public int compare(T o1, T o2) {
    return Utils.compare(o1, o2);
  }
}
