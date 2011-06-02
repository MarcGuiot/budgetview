package org.uispec4j.utils;

import com.sun.xml.internal.xsom.impl.Ref;

import java.util.HashSet;
import java.util.Set;

public class IntSet {

  private Set<Integer> set = new HashSet<Integer>();

  public void add(int i) {
    set.add(i);
  }

  public void addAll(int[] array) {
    for (int element : array) {
      set.add(element);
    }
  }

  public int size() {
    return set.size();
  }

  public int[] toIntArray() {
    int[] result = new int[set.size()];
    int i = 0;
    for (Integer integer : set) {
      result[i++] = integer;
    }
    return result;
  }
}
