package com.budgetview.triggers;

import com.budgetview.model.Account;
import com.budgetview.shared.model.AccountType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import java.util.HashSet;
import java.util.Set;

public class SameAccountChecker {
  private Set<Integer> accounts = new HashSet<Integer>();
  private Key summaryAccountId;

  public SameAccountChecker(Integer accountType, GlobRepository repository) {
    update(repository, accountType);
  }

  private void update(GlobRepository repository, Integer accountType) {
    if (AccountType.MAIN.getId().equals(accountType)) {
      summaryAccountId = Account.MAIN_SUMMARY_KEY;
    }
    else if (AccountType.SAVINGS.getId().equals(accountType)) {
      summaryAccountId = Account.SAVINGS_SUMMARY_KEY;
    }
    GlobList accounts = repository.getAll(Account.TYPE);
    for (Glob account : accounts) {
      if (accountType.equals(account.get(Account.ACCOUNT_TYPE)) &&
          account.get(Account.ID) != Account.EXTERNAL_ACCOUNT_ID &&
          Account.isNotDeferred(account)) {
        this.accounts.add(account.get(Account.ID));
      }
    }
  }

  public boolean isSame(Integer accountId) {
    return accounts.contains(accountId);
  }

  public Key getSummary() {
    return summaryAccountId;
  }
}
