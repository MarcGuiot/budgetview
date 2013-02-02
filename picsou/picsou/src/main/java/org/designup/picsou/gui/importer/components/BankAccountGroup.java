package org.designup.picsou.gui.importer.components;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;

public class BankAccountGroup {
  private final Glob bank;
  private final GlobList accounts = new GlobList();

  public BankAccountGroup(Glob bank) {
    this.bank = bank;
  }

  public Glob getBank() {
    return bank;
  }

  public void add(Glob account) {
    getAccounts().add(account);
  }

  public GlobList getAccounts() {
    return accounts;
  }
}
