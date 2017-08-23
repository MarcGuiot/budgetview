package com.budgetview.triggers;

import com.budgetview.model.Account;
import com.budgetview.model.RealAccount;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldIn;
import static org.globsframework.model.utils.GlobMatchers.linkedTo;
import static org.globsframework.model.utils.GlobUtils.getValues;

public class RealAccountTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsDeletions(Account.TYPE)) {
      for (Key accountKey : changeSet.getDeleted(Account.TYPE)) {
        for (Glob realAccount : repository.getAll(RealAccount.TYPE, linkedTo(accountKey, RealAccount.ACCOUNT))) {
          if (realAccount.get(RealAccount.PROVIDER_ACCOUNT_ID) != null) {
            repository.update(realAccount,
                              value(RealAccount.ACCOUNT, null),
                              value(RealAccount.ENABLED, false));
          }
          else {
            repository.delete(realAccount);
          }
        }
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Account.TYPE)) {
      for (Glob realAccount : repository.getAll(RealAccount.TYPE)) {
        Integer accountId = realAccount.get(RealAccount.ACCOUNT);
        if (accountId == null || !repository.contains(Key.create(Account.TYPE, accountId))) {
          repository.delete(realAccount);
        }
      }
    }
  }
}
