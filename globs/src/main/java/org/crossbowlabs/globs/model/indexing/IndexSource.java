package org.crossbowlabs.globs.model.indexing;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.Glob;

public interface IndexSource {
  Iterable<Glob> getGlobs(GlobType globType);
}
