package org.globsframework.model.utils;

import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

public interface GlobListFunctor {
  void run(GlobList list, GlobRepository repository);
}
