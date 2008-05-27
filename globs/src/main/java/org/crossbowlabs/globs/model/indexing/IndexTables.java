package org.crossbowlabs.globs.model.indexing;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.Glob;

public interface IndexTables {
  void add(Object newValue, Glob glob, Field field, Object oldValue);

  void add(Glob glob);

  IndexTables add(IndexedTable indexedTable);

  void remove(Glob glob);

  void remove(Field field, Object oldValue, Glob glob);

  void removeAll();
}
