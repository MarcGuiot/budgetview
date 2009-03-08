package org.designup.picsou.gui.transactions.categorization;

import org.designup.picsou.gui.categories.CategoryChooserCallback;
import org.designup.picsou.model.Category;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class TransactionCategoryChooserCallback implements CategoryChooserCallback {
  public Set<Integer> getPreselectedCategoryIds() {

    Set<Integer> categoryIds = new HashSet<Integer>();
    for (Glob transaction : getReferenceTransactions()) {
      Integer categoryId = transaction.get(Category.ID);
      if (categoryId == null) {
        categoryIds.add(Category.NONE);
      }
      else {
        categoryIds.add(categoryId);
      }
    }
    return categoryIds;
  }

  public Set<Integer> getUnUncheckable() {
    return Collections.emptySet();
  }

  protected abstract GlobList getReferenceTransactions();

  protected abstract GlobRepository getRepository();
}
