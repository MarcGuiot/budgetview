package org.designup.picsou.gui.accounts.utils;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountCardType;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.Month;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;

import java.util.Date;
import java.util.SortedSet;

public class AccountMatchers {

  public static GlobMatcher userOrSummaryMainAccounts(final Integer userAccountId) {
    return new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        Integer accountId = account.get(Account.ID);
        return Utils.equal(accountId, userAccountId) || Utils.equal(accountId, Account.MAIN_SUMMARY_ACCOUNT_ID);
      }
    };
  }

  public static GlobMatcher userOrSummaryMainAccounts() {
    return new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        return Account.isUserOrSummaryMain(account) && !account.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId());
      }
    };
  }

  public static GlobMatcher userOrAllAccounts() {
    return new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        return Account.isUserCreatedAccountOrAll(account);
      }
    };
  }

  public static GlobMatcher activeUserCreatedAccounts(final SortedSet<Integer> monthIds) {
    if (monthIds.isEmpty()) {
      return GlobMatchers.NONE;
    }
    return new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        if (!Account.isUserCreatedAccount(account)) {
          return false;
        }
        if (account.get(Account.CLOSED_DATE) != null) {
          Integer endMonth = Month.getMonthId(account.get(Account.CLOSED_DATE));
          if (endMonth < monthIds.first()) {
            return false;
          }
        }
        if (account.get(Account.OPEN_DATE) != null) {
          Integer openMonth = Month.getMonthId(account.get(Account.OPEN_DATE));
          if (openMonth > monthIds.last()) {
            return false;
          }
        }
        return true;
      }
    };
  }

  public static GlobMatcher activeMainAccountsForPlannedTransactions(final Integer monthId) {
    return new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        if (account.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())){
          return false;
        }
        if (!Account.isUserCreatedMainAccount(account)) {
          return false;
        }
        if (account.get(Account.CLOSED_DATE) != null) {
          Integer endMonth = Month.getMonthId(account.get(Account.CLOSED_DATE));
          if (endMonth < monthId) {
            return false;
          }
        }
        if (account.get(Account.OPEN_DATE) != null) {
          Integer openMonth = Month.getMonthId(account.get(Account.OPEN_DATE));
          if (openMonth > monthId) {
            return false;
          }
        }
        return true;
      }
    };
  }

  public static GlobMatcher activeUserCreatedMainAccounts(final SortedSet<Integer> monthIds) {
    if (monthIds.isEmpty()) {
      return GlobMatchers.NONE;
    }
    return new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        if (!Account.isUserCreatedMainAccount(account)) {
          return false;
        }
        if (account.get(Account.CLOSED_DATE) != null) {
          Integer endMonth = Month.getMonthId(account.get(Account.CLOSED_DATE));
          if (endMonth < monthIds.first()) {
            return false;
          }
        }
        if (account.get(Account.OPEN_DATE) != null) {
          Integer openMonth = Month.getMonthId(account.get(Account.OPEN_DATE));
          if (openMonth > monthIds.last()) {
            return false;
          }
        }
        return true;
      }
    };
  }

  public static GlobMatcher activeUserCreatedSavingsAccounts(final SortedSet<Integer> monthIds) {
    if (monthIds.isEmpty()) {
      return GlobMatchers.NONE;
    }
    return new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        if (!Account.isUserCreatedSavingsAccount(account)) {
          return false;
        }
        if (account.get(Account.CLOSED_DATE) != null) {
          Integer endMonth = Month.getMonthId(account.get(Account.CLOSED_DATE));
          if (endMonth < monthIds.first()) {
            return false;
          }
        }
        if (account.get(Account.OPEN_DATE) != null) {
          Integer openMonth = Month.getMonthId(account.get(Account.OPEN_DATE));
          if (openMonth > monthIds.last()) {
            return false;
          }
        }
        return true;
      }
    };
  }

  public static GlobMatcher accountsNotClosedAsOf(final Date date) {
    return new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        Date endDate = account.get(Account.CLOSED_DATE);
        return endDate == null || endDate.after(date);
      }
    };
  }

  public static GlobMatcher userCreatedAccounts() {
    return new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        return Account.isUserCreatedAccount(account);
      }
    };
  }

  public static GlobMatcher userCreatedAccounts(final AccountType type) {
    return new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        return Account.isUserCreatedAccount(account) && type.getId().equals(account.get(Account.ACCOUNT_TYPE));
      }
    };
  }

  public static GlobMatcher userCreatedMainAccounts() {
    return new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        return Account.isUserCreatedMainAccount(account);
      }
    };
  }

  public static GlobMatcher userCreatedSavingsAccounts() {
    return new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        return Account.isUserCreatedSavingsAccount(account);
      }
    };
  }
}
