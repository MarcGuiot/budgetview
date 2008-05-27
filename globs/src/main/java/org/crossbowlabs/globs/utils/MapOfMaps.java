package org.crossbowlabs.globs.utils;

import java.util.*;

public class MapOfMaps<KEY1, KEY2, VALUE> {
  private Map<KEY1, Map<KEY2, VALUE>> maps = new HashMap<KEY1, Map<KEY2, VALUE>>();

  public VALUE put(KEY1 key1, KEY2 key2, VALUE value) {
    Map<KEY2, VALUE> map = maps.get(key1);
    if (map == null) {
      map = new HashMap<KEY2, VALUE>();
      maps.put(key1, map);
    }
    return map.put(key2, value);
  }

  public VALUE get(KEY1 key1, KEY2 key2) {
    Map<KEY2, VALUE> map = maps.get(key1);
    if (map == null) {
      return null;
    }
    return map.get(key2);
  }

  public Map<KEY2, VALUE> get(KEY1 key1) {
    Map<KEY2, VALUE> map = maps.get(key1);
    if (map != null) {
      return map;
    }
    else {
      return Collections.emptyMap();
    }
  }

  public Iterator<VALUE> iterator() {
    return new MapOfMapIterator();
  }

  public VALUE remove(KEY1 key1, KEY2 key2) {
    Map<KEY2, VALUE> map = maps.get(key1);
    if (map != null) {
      return map.remove(key2);
    }
    return null;
  }

  public Collection<VALUE> values(KEY1 key1) {
    Map<KEY2, VALUE> map = maps.get(key1);
    if (map != null) {
      return map.values();
    }
    return Collections.EMPTY_LIST;
  }

  public Collection<VALUE> values() {
    List<VALUE> result = new ArrayList<VALUE>();
    for (Map map : maps.values()) {
      result.addAll(map.values());
    }
    return result;
  }

  public Set<KEY1> keys() {
    return maps.keySet();
  }

  public boolean containsKey(KEY1 key1, KEY2 key2) {
    return maps.containsKey(key1) && maps.get(key1).containsKey(key2);
  }

  public void removeAll(KEY1 key1) {
    maps.remove(key1);
  }

  private class MapOfMapIterator implements Iterator<VALUE> {
    Iterator<Map<KEY2, VALUE>> iterator1;
    Iterator<VALUE> iterator2;
    VALUE value;

    public MapOfMapIterator() {
      iterator1 = maps.values().iterator();
    }

    public boolean hasNext() {
      if (iterator2 != null && iterator2.hasNext()) {
        return true;
      }
      while (iterator1.hasNext() && (iterator2 == null || !iterator2.hasNext())) {
        iterator2 = iterator1.next().values().iterator();
      }
      return iterator2.hasNext();
    }

    public VALUE next() {
      return iterator2.next();
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}

