package org.designup.picsou.gui.accounts.utils;

import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;

public abstract class AccountTypeSelector {
  private String label;

  public AccountTypeSelector(String labelKey) {
    this.label = Lang.get(labelKey);
  }

  public abstract void apply();

  public abstract boolean isApplied(Glob account);

  public String toString() {
    return label;
  }
}
