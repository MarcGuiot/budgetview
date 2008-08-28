package org.designup.picsou.gui.categories;

import org.globsframework.model.GlobList;

import java.util.Set;

public interface CategoryChooserCallback {
  void processSelection(GlobList categories);

  Set<Integer> getPreselectedCategoryIds();
}
