package org.crossbowlabs.globs.model.indexing.indices;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.EmptyGlobList;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.indexing.IndexedTable;
import org.crossbowlabs.globs.utils.MultiMap;

import java.util.List;

public class DefaultNotUniqueIndex implements IndexedTable {
  private Field field;
  private MultiMap<Object, Glob> index = new MultiMap<Object, Glob>();

  public DefaultNotUniqueIndex(Field field) {
    this.field = field;
  }

  public boolean remove(Field field, Object value, Glob glob) {
    index.removeValue(value, glob);
    return false;
  }

  public void add(Field field, Object newValue, Object oldValue, Glob glob) {
    index.removeValue(oldValue, glob);
    index.putUnique(newValue, glob);
  }

  public void add(Glob glob) {
    Object value = glob.getValue(field);
    index.putUnique(value, glob);
  }

  public GlobList findByIndex(Object value) {
    List<Glob> globs = index.get(value);
    if (globs == null) {
      return new EmptyGlobList();
    }
    return new GlobList(globs);
  }

  public boolean remove(Glob glob) {
    Object value = glob.getValue(field);
    return index.removeValue(value, glob);
  }

  public void removeAll() {
    index.clear();
  }
}
