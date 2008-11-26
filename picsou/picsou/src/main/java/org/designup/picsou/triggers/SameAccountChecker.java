package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import java.util.HashSet;
import java.util.Set;

public class SameAccountChecker {
  private Set<Integer> accounts = new HashSet<Integer>();
  private Key summaryAccountId;

  public SameAccountChecker(Glob account, GlobRepository repository) {
    update(repository, account.get(Account.ACCOUNT_TYPE));
  }

  public SameAccountChecker(Integer accountType, GlobRepository repository) {
    update(repository, accountType);
  }

  private void update(GlobRepository repository, Integer accountType) {
    if (accountType.equals(AccountType.MAIN.getId())) {
      summaryAccountId = Account.MAIN_SUMMARY_KEY;
    }
    else {
      summaryAccountId = Account.SAVINGS_SUMMARY_KEY;
    }
    GlobList accounts = repository.getAll(Account.TYPE);
    for (Glob tmp : accounts) {
      if (tmp.get(Account.ACCOUNT_TYPE).equals(accountType)) {
        this.accounts.add(tmp.get(Account.ID));
      }
    }
  }

  public static SameAccountChecker getSameAsSavings(GlobRepository repository) {
    return new SameAccountChecker(AccountType.SAVINGS.getId(), repository);
  }

  public static SameAccountChecker getSameAsMain(GlobRepository repository) {
    return new SameAccountChecker(AccountType.MAIN.getId(), repository);
  }

  public boolean isSame(Integer accountId) {
    return accounts.contains(accountId);
  }

  public Key getSummary() {
    return summaryAccountId;
  }
}
