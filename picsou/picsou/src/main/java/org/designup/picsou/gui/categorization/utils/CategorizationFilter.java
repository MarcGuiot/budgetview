package org.designup.picsou.gui.categorization.utils;

import org.globsframework.model.GlobList;
import org.globsframework.model.utils.GlobMatcher;

public interface CategorizationFilter extends GlobMatcher {
  void filterForTransactions(GlobList transactions);
}
