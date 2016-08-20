package com.budgetview.bank;

import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;

/** @deprecated  */
public interface BankPlugin {

  boolean apply(Glob importedAccount, Glob newAccount, GlobList transactions, ReadOnlyGlobRepository referenceRepository,
                GlobRepository localRepository, MutableChangeSet changeSet);

  void postApply(GlobList list, Glob account, GlobRepository repository, GlobRepository localRepository, ChangeSet set);

  int getVersion();
}
