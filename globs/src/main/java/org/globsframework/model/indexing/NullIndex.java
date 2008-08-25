package org.globsframework.model.indexing;

import org.globsframework.metamodel.Field;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

public class NullIndex implements GlobRepository.MultiFieldIndexed {
  public static GlobRepository.MultiFieldIndexed INSTANCE = new NullIndex();

  public GlobList getGlobs() {
    return new GlobList();
  }

  public GlobList findByIndex(Object value) {
    return new GlobList();
  }

  public GlobRepository.MultiFieldIndexed findByIndex(Field field, Object value) {
    return this;
  }
}
