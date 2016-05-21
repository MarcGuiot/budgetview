package com.budgetview.gui.description.stringifiers;

import com.budgetview.model.Account;
import com.budgetview.model.AccountType;
import com.budgetview.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;

import java.util.Set;

public class AccountListStringifier implements GlobListStringifier {

  private String defaultLabel = "";

  public AccountListStringifier() {
  }

  public AccountListStringifier(String defaultLabel) {
    this.defaultLabel = defaultLabel;
  }

  public String toString(GlobList accounts, GlobRepository repository) {
    if (accounts.isEmpty()) {
      return defaultLabel;
    }

    if (accounts.size() == 1) {
      Glob account = accounts.getFirst();
      Integer accountId = account.get(Account.ID);
      switch (accountId) {
        case Account.MAIN_SUMMARY_ACCOUNT_ID:
          return Lang.get("accountList.main");
        case Account.SAVINGS_SUMMARY_ACCOUNT_ID:
          return Lang.get("accountList.savings");
        case Account.ALL_SUMMARY_ACCOUNT_ID:
          return Lang.get("accountList.all");
        case Account.EXTERNAL_ACCOUNT_ID:
          return Lang.get("accountList.external");
      }
      return account.get(Account.NAME);
    }

    Set<Integer> accountTypes = accounts.getValueSet(Account.ACCOUNT_TYPE);
    if (accountTypes.size() == 1) {
      switch (AccountType.get(accountTypes.iterator().next())) {
        case MAIN:
          return Lang.get("accountList.multiple", accounts.size());
        case SAVINGS:
          return Lang.get("accountList.multiple", accounts.size());
      }
    }

    return Lang.get("accountList.multiple", accounts.size());
  }
}
