package org.designup.picsou.gui.description;

import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;

public class TransactionCategoriesStringifier extends AbstractGlobStringifier {
  private final GlobStringifier categoryStringifier;

  public TransactionCategoriesStringifier(GlobStringifier categoryStringifier) {
    this.categoryStringifier = categoryStringifier;
  }

  public String toString(Glob transaction, GlobRepository repository) {
    return Transaction.stringifyCategories(transaction, repository, categoryStringifier);
  }
}
