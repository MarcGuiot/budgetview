package org.designup.picsou.gui.accounts;

import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;

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
