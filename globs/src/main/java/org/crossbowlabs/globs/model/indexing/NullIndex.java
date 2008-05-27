package org.crossbowlabs.globs.model.indexing;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;

public class NullIndex implements GlobRepository.MultiFieldIndexed {
  public static GlobRepository.MultiFieldIndexed INSTANCE = new NullIndex();

  public GlobList getGlobs() {
    return GlobList.EMPTY;
  }

  public GlobList findByIndex(Object value) {
    return GlobList.EMPTY;
  }

  public GlobRepository.MultiFieldIndexed findByIndex(Field field, Object value) {
    return this;
  }
}
