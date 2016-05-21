package com.budgetview.model.upgrade;

import com.budgetview.model.Account;
import com.budgetview.model.AccountCardType;
import com.budgetview.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;

public class DeferredAccountUpgradeV40 {

  public static void run(GlobRepository repository) {
    GlobList deferredAccounts = repository.getAll(Account.TYPE, GlobMatchers.and(GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()),
                                                                    GlobMatchers.isNotNull(Account.DEFERRED_TARGET_ACCOUNT)));
    for (Glob account : deferredAccounts) {
      final Integer mainAccount = account.get(Account.DEFERRED_TARGET_ACCOUNT);
      if (Account.isUserCreatedAccount(mainAccount)){
        repository.safeApply(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.ACCOUNT, account.get(Account.ID)),
                             new GlobFunctor() {
                               public void run(Glob glob, GlobRepository repository) throws Exception {
                                 repository.update(glob.getKey(), Transaction.ACCOUNT, mainAccount);
                               }
                             });
      }
    }
  }
}
