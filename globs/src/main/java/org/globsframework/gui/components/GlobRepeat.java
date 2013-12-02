package org.globsframework.gui.components;

import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.GlobList;

public interface GlobRepeat {

  void setFilter(GlobMatcher matcher);

  boolean isEmpty();

  int size();

  GlobList getCurrentGlobs();

  void addListener(GlobRepeatListener listener);

  void removeListener(GlobRepeatListener listener);
}
