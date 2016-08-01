package com.budgetview.triggers;

import com.budgetview.model.RealAccount;
import com.budgetview.model.Account;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;
import static org.globsframework.model.utils.GlobUtils.getValues;

public class RealAccountTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsDeletions(Account.TYPE)) {
      repository.delete(RealAccount.TYPE,
                        fieldIn(RealAccount.ACCOUNT, getValues(changeSet.getDeleted(Account.TYPE), Account.ID)));
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
