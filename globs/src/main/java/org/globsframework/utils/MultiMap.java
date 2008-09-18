package org.globsframework.utils;

import java.util.*;

public class MultiMap<K, V> {
  private Map<K, List<V>> map = new HashMap<K, List<V>>();

  public void put(K key, V value) {
    List<V> values = getOrCreateList(key);
    values.add(value);
  }

  public void putAll(K key, Collection<V> values) {
    List<V> list = getOrCreateList(key);
    list.addAll(values);
  }

  private List<V> getOrCreateList(K key) {
    List<V> values = map.get(key);
    if (values == null) {
      values = new ArrayList<V>();
      map.put(key, values);
    }
    return values;
  }

  public int size() {
    int result = 0;
    for (List<V> list : map.values()) {
      result += list.size();
    }
    return result;
  }

  public List<V> get(K key) {
    List<V> values = map.get(key);
    if (values == null) {
      return Collections.EMPTY_LIST;
    }
    return Collections.unmodifiableList(values);
  }

  public Set<Map.Entry<K, List<V>>> values() {
    return map.entrySet();
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Map.Entry<K, List<V>> entry : map.entrySet()) {
      builder.append(entry.getKey());
      builder.append(": ");
      builder.append(entry.getValue());
      builder.append(Strings.LINE_SEPARATOR);
    }
    return builder.toString();
  }

  public Set<K> keySet() {
    return map.keySet();
  }

  public List<V> remove(K k) {
    return map.remove(k);
  }

  public boolean removeValue(V value) {
    boolean removed = false;
    for (List<V> list : map.values()) {
      removed |= list.remove(value);
    }
    return removed;
  }

  public boolean removeValue(K key, V value) {
    List<V> list = map.get(key);
    if (list != null) {
      boolean find = list.remove(value);
      if (list.isEmpty()) {
        map.remove(key);
      }
      return find;
    }
    return false;
  }

  public void putUnique(K key, V value) {
    List<V> list = map.get(key);
    if (list != null) {
      boolean contains = false;
      Utils.beginRemove();
      contains = list.contains(value);
      Utils.endRemove();
      if (!contains) {
        list.add(value);
      }
      else {
        throw new RuntimeException("putUnique fail for " + value);
      }
    }
    else {
      List<V> values = new ArrayList<V>();
      map.put(key, values);
      values.add(value);
    }
  }

  public void clear() {
    map.clear();
  }

  public boolean containsKey(K key) {
    List<V> result = map.get(key);
    return !(result == null || result.isEmpty());
  }

  public MultiMap<K, V> duplicate() {
    MultiMap<K, V> result = new MultiMap<K, V>();
    for (Map.Entry<K, List<V>> entry : map.entrySet()) {
      final K key = entry.getKey();
      final ArrayList<V> list = new ArrayList<V>(entry.getValue());
      result.map.put(key, list);
    }
    return result;
  }
}
