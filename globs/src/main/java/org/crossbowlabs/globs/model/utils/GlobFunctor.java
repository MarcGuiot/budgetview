package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.model.Glob;

public interface GlobFunctor {
  void run(Glob glob) throws Exception;
}
