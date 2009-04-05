package org.globsframework.model;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.index.Index;
import org.globsframework.metamodel.index.MultiFieldIndex;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.exceptions.ItemAmbiguity;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;

public interface ReadOnlyGlobRepository {
  Glob find(Key key);

  Glob get(Key key)
    throws ItemNotFound;

  GlobList getAll(GlobType... type);

  GlobList getAll(GlobType type, GlobMatcher matcher);

  void apply(GlobType type, GlobMatcher matcher, GlobFunctor callback) throws Exception;

  void safeApply(GlobType type, GlobMatcher matcher, GlobFunctor callback);

  Glob findUnique(GlobType type, FieldValue... values)
    throws ItemAmbiguity;

  Glob findUnique(GlobType type, GlobMatcher matcher)
    throws ItemAmbiguity;

  SortedSet<Glob> getSorted(GlobType globType, Comparator<Glob> comparator, GlobMatcher matcher);

  GlobList findByIndex(Index index, Object value);

  MultiFieldIndexed findByIndex(MultiFieldIndex uniqueIndex, Field field, Object value);

  Set<GlobType> getTypes();

  Glob findLinkTarget(Glob source, Link link);

  GlobList findLinkedTo(Glob target, Link link);

  interface MultiFieldIndexed {
    GlobList getGlobs();

    GlobList findByIndex(Object value);

    MultiFieldIndexed findByIndex(Field field, Object value);

    void apply(GlobFunctor functor, GlobRepository repository) throws Exception;
  }
}
