package org.crossbowlabs.globs.model.indexing;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;

public interface IndexedTable {
  void add(Glob glob);

  void add(Field field, Object newValue, Object oldValue, Glob glob);

  GlobList findByIndex(Object value);

  boolean remove(Field field, Object value, Glob glob);

  boolean remove(Glob glob);

  void removeAll();
}
