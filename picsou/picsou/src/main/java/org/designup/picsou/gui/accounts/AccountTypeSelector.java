package org.designup.picsou.gui.accounts;

import org.designup.picsou.utils.Lang;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public abstract class AccountTypeSelector {
  private String label;

  AccountTypeSelector(String labelKey) {
    this.label = Lang.get(labelKey);
  }

  protected abstract void apply();

  protected abstract boolean isApplied(Glob account);

  public String toString() {
    return label;
  }
  
}
