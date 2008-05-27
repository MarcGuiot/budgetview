package org.crossbowlabs.globs.model.indexing.indices;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.indexing.IndexTables;
import org.crossbowlabs.globs.model.indexing.IndexedTable;

import java.util.ArrayList;
import java.util.List;

public class ManyIndexTables implements IndexTables {
  private List<IndexedTable> indexedTables = new ArrayList<IndexedTable>();

  public ManyIndexTables(IndexedTable firstIndexedTable, IndexedTable secondIndexedTable, IndexedTable indexedTable) {
    indexedTables.add(firstIndexedTable);
    indexedTables.add(secondIndexedTable);
    indexedTables.add(indexedTable);
  }

  public void remove(Field field, Object oldValue, Glob glob) {
    for (IndexedTable indexedTable : indexedTables) {
      indexedTable.remove(field, oldValue, glob);
    }
  }

  public void add(Object newValue, Glob glob, Field field, Object oldValue) {
    for (IndexedTable indexedTable : indexedTables) {
      indexedTable.add(field, newValue, oldValue, glob);
    }
  }

  public void add(Glob glob) {
    for (IndexedTable indexedTable : indexedTables) {
      indexedTable.add(glob);
    }
  }

  public void remove(Glob glob) {
    for (IndexedTable indexedTable : indexedTables) {
      indexedTable.remove(glob);
    }
  }

  public IndexTables add(IndexedTable indexedTable) {
    indexedTables.add(indexedTable);
    return this;
  }

  public void removeAll() {
    for (IndexedTable indexedTable : indexedTables) {
      indexedTable.removeAll();
    }
  }
}
