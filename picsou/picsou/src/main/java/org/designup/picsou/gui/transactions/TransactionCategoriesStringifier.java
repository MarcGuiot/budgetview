package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.model.format.utils.AbstractGlobStringifier;
import org.designup.picsou.model.Transaction;

public class TransactionCategoriesStringifier extends AbstractGlobStringifier {
  private final GlobStringifier categoryStringifier;

  public TransactionCategoriesStringifier(GlobStringifier categoryStringifier) {
    this.categoryStringifier = categoryStringifier;
  }

  public String toString(Glob transaction, GlobRepository repository) {
    return Transaction.stringifyCategories(transaction, repository, categoryStringifier);
  }
}
