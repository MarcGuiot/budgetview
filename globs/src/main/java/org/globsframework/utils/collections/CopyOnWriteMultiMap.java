package org.globsframework.utils.collections;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CopyOnWriteMultiMap<K, V> extends MultiMap<K, V> {
  protected List<V> createNewList() {
    return new CopyOnWriteArrayList<V>();
  }
}