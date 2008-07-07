package org.designup.picsou.gui.transactions.categorization;

import org.globsframework.model.Glob;

import java.util.Set;

public interface CategoryChooserCallback {
  void categorySelected(Glob category);

  Set<Integer> getPreselectedCategoryIds();
}
