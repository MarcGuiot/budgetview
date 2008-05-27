package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;

public class GlobKeyMatcher implements GlobMatcher {
  private Key key;

  public GlobKeyMatcher(Key key) {
    this.key = key;
  }

  public boolean matches(Glob item, GlobRepository repository) {
    return (item != null) && item.getKey().equals(key);
  }
}
