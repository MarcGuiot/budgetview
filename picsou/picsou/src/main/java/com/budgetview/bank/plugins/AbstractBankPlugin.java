package com.budgetview.bank.plugins;

import com.budgetview.bank.BankPlugin;
import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;

public abstract class AbstractBankPlugin implements BankPlugin {

  public boolean apply(Glob importedAccount, Glob account, GlobList transactions, ReadOnlyGlobRepository referenceRepository,
                       GlobRepository localRepository, MutableChangeSet changeSet) {
    return false;
  }

  public void postApply(GlobList transactions, Glob account, GlobRepository repository, GlobRepository localRepository, ChangeSet set) {
  }
}
