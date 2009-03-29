package org.globsframework.model.indexing.indices;

import org.globsframework.metamodel.Field;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;

import java.util.HashMap;
import java.util.Map;

public class NotUniqueLeafLevelIndex implements UpdatableMultiFieldIndex, GlobRepository.MultiFieldIndexed {
  private Map<Object, GlobList> indexedGlob = new HashMap<Object, GlobList>();
  private Field field;

  public NotUniqueLeafLevelIndex(Field field) {
    this.field = field;
  }

  public GlobList getGlobs() {
    GlobList globs = new GlobList();
    for (GlobList glob : indexedGlob.values()) {
      globs.addAll(glob);
    }
    return globs;
  }

  public void callOnGlobs(GlobFunctor functor, GlobRepository repository) throws Exception {
    for (GlobList globList : indexedGlob.values()) {
      for (Glob glob : globList) {
        functor.run(glob, repository);
      }
    }
  }

  public GlobList findByIndex(Object value) {
    GlobList glob = indexedGlob.get(value);
    if (glob == null) {
      return GlobList.EMPTY;
    }
    else {
      return new GlobList(glob);
    }
  }

  public boolean remove(Glob glob) {
    Object value = glob.getValue(this.field);
    GlobList globList = indexedGlob.get(value);
    if (globList != null) {
      globList.remove(glob);
      if (globList.isEmpty()) {
        indexedGlob.remove(value);
      }
    }
    return indexedGlob.isEmpty();
  }

  public void removeAll() {
    indexedGlob.clear();
  }

  public GlobRepository.MultiFieldIndexed findByIndex(Field field, final Object value) {
    return new GlobRepository.MultiFieldIndexed() {
      public GlobList getGlobs() {
        return NotUniqueLeafLevelIndex.this.findByIndex(value);
      }

      public void callOnGlobs(GlobFunctor functor, GlobRepository repository) throws Exception {
        GlobList globs = indexedGlob.get(value);
        if (globs == null){
          return;
        }
        for (Glob glob : globs) {
          functor.run(glob, repository);
        }
      }

      public GlobList findByIndex(Object value) {
        return new GlobList();
      }

      public GlobRepository.MultiFieldIndexed findByIndex(Field field, Object value) {
        return null;
      }
    };
  }

  public void add(Field field, Object newValue, Object oldValue, Glob glob) {
    if (field == this.field) {
      GlobList oldGlobList = indexedGlob.get(oldValue);
      if (oldGlobList != null) {
        oldGlobList.remove(glob);
        if (oldGlobList.isEmpty()) {
          indexedGlob.remove(oldValue);
        }
      }
    }
    add(glob);
  }

  public boolean remove(Field field, Object value, Glob glob) {
    if (this.field == field) {
      GlobList oldGlob = indexedGlob.get(value);
      if (oldGlob != null) {
        oldGlob.remove(glob);
        if (oldGlob.isEmpty()) {
          indexedGlob.remove(value);
        }
      }
    }
    else {
      Object oldValue = glob.getValue(this.field);
      GlobList oldGlob = indexedGlob.get(oldValue);
      if (oldGlob != null) {
        oldGlob.remove(glob);
        if (oldGlob.isEmpty()) {
          indexedGlob.remove(value);
        }
      }
    }
    return indexedGlob.isEmpty();
  }

  public void add(Glob glob) {
    Object value = glob.getValue(field);
    GlobList globList = indexedGlob.get(value);
    if (globList == null) {
      globList = new GlobList();
      indexedGlob.put(value, globList);
    }
    globList.add(glob);
  }
}
