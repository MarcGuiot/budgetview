package org.globsframework.model.utils;

import org.globsframework.model.Glob;

public interface GlobFunctor {
  void run(Glob glob) throws Exception;
}
