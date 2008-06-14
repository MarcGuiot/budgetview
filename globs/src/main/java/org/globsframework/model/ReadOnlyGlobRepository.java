package org.globsframework.model;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.index.Index;
import org.globsframework.metamodel.index.MultiFieldIndex;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.exceptions.ItemAmbiguity;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.util.Set;

public interface ReadOnlyGlobRepository {
  Glob find(Key key);

  Glob get(Key key)
    throws ItemNotFound;

  GlobList getAll(GlobType... type);

  GlobList getAll(GlobType type, GlobMatcher matcher);

  Glob findUnique(GlobType type, FieldValue... values)
    throws ItemAmbiguity;

  Glob findUnique(GlobType type, GlobMatcher matcher)
    throws ItemAmbiguity;

  GlobList findByIndex(Index index, Object value);

  MultiFieldIndexed findByIndex(MultiFieldIndex uniqueIndex, Field field, Object value);

  Set<GlobType> getTypes();

  Glob findLinkTarget(Glob source, Link link);

  GlobList findLinkedTo(Glob target, Link link);

  interface MultiFieldIndexed {
    GlobList getGlobs();

    GlobList findByIndex(Object value);

    GlobRepository.MultiFieldIndexed findByIndex(Field field, Object value);
  }
}
