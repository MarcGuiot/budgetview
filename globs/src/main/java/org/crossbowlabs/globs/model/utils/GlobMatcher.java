package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;

import java.io.Serializable;

/**
 * @see GlobMatchers
 */
public interface GlobMatcher extends Serializable {
  boolean matches(Glob item, GlobRepository repository);
}
