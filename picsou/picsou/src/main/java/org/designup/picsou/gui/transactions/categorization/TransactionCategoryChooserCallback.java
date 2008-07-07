package org.designup.picsou.gui.transactions.categorization;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.Category;

import java.util.Set;
import java.util.HashSet;

public abstract class TransactionCategoryChooserCallback implements CategoryChooserCallback  {
  public Set<Integer> getPreselectedCategoryIds() {
    Set<Integer> categoryIds = new HashSet<Integer>();
    for (Glob transaction : getReferenceTransactions()) {
      GlobList categories = Transaction.getCategories(transaction, getRepository());
      if (categories.size() == 0) {
        categoryIds.add(Category.NONE);
      }
      for (Glob category : categories) {
        categoryIds.add(category.get(Category.ID));
      }
    }
    return categoryIds;
  }

  protected abstract GlobList getReferenceTransactions();

  protected abstract GlobRepository getRepository();
}
