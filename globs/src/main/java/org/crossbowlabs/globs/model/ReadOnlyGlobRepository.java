package org.crossbowlabs.globs.model;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.metamodel.index.Index;
import org.crossbowlabs.globs.metamodel.index.MultiFieldIndex;
import org.crossbowlabs.globs.model.utils.GlobMatcher;
import org.crossbowlabs.globs.utils.exceptions.ItemAmbiguity;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;

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
