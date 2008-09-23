package org.designup.picsou.gui.components.expansion;

import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.Glob;

public interface ExpandableTable {

  void setFilter(GlobMatcher matcher);

  void select(Glob glob);

  Glob getSelectedGlob();
}
