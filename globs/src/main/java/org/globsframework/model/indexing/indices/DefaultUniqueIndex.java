package org.globsframework.model.indexing.indices;

import org.globsframework.metamodel.Field;
import org.globsframework.model.EmptyGlobList;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.indexing.IndexedTable;

import java.util.HashMap;
import java.util.Map;

public class DefaultUniqueIndex implements IndexedTable {
  private Field field;
  private Map<Object, Glob> index = new HashMap<Object, Glob>();

  public DefaultUniqueIndex(Field field) {
    this.field = field;
  }

  public boolean remove(Field field, Object value, Glob glob) {
    if (index.get(value) == glob) {
      index.remove(value);
    }
    return false;
  }

  public void add(Field field, Object newValue, Object oldValue, Glob glob) {
    Glob oldGlob = index.remove(oldValue);
    if (oldGlob != null && oldGlob != glob) {
      index.put(oldValue, oldGlob);
    }
    index.put(newValue, glob);
  }

  public void add(Glob glob) {
    index.put(glob.getValue(field), glob);
  }

  public GlobList findByIndex(Object value) {
    Glob glob = index.get(value);
    if (glob == null) {
      return new GlobList();
    }
    return new GlobList(glob);
  }

  public boolean remove(Glob glob) {
    Object value = glob.getValue(field);
    if (index.get(value) == glob) {
      index.remove(value);
    }
    return false;
  }

  public void removeAll() {
    index.clear();
  }
}
