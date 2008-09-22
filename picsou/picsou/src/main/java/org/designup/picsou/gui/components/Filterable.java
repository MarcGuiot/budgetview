package org.designup.picsou.gui.components;

import org.globsframework.model.utils.GlobMatcher;

public interface Filterable {
  void setFilter(GlobMatcher matcher);
}
