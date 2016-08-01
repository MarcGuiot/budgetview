package com.budgetview.shared.utils;

import com.budgetview.shared.model.AccountEntity;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.Utils;

public class AccountEntityMatchers {
  public static GlobMatcher main() {
    return new GlobMatcher() {
      public boolean matches(Glob accountEntity, GlobRepository repository) {
        return accountEntity.isTrue(AccountEntity.IS_USER_ACCOUNT)
               && Utils.equal(accountEntity.get(AccountEntity.ACCOUNT_TYPE),
                              AccountEntity.ACCOUNT_TYPE_MAIN);
      }
    };
  }

  public static GlobMatcher savings() {
    return new GlobMatcher() {
      public boolean matches(Glob accountEntity, GlobRepository repository) {
        return accountEntity.isTrue(AccountEntity.IS_USER_ACCOUNT)
               && Utils.equal(accountEntity.get(AccountEntity.ACCOUNT_TYPE),
                              AccountEntity.ACCOUNT_TYPE_SAVINGS);
      }
    };
  }
}
