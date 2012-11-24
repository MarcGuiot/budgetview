package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountPositionError;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;

import java.util.Set;

public class AccountDeleteTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (!changeSet.containsCreationsOrDeletions(Account.TYPE)) {
      return;
    }

    Set<Key> deletedAccountKeys = changeSet.getDeleted(Account.TYPE);
    for (Key accountKey : deletedAccountKeys) {
      repository.delete(Transaction.TYPE, GlobMatchers.linkedTo(accountKey, Transaction.ACCOUNT));
      repository.delete(Series.TYPE, or(linkedTo(accountKey, Series.FROM_ACCOUNT),
                                        linkedTo(accountKey, Series.TO_ACCOUNT)));
      repository.delete(AccountPositionError.TYPE, linkedTo(accountKey, AccountPositionError.ACCOUNT));
    }

    for (Key accountKey : changeSet.getCreated(Account.TYPE)) {
      repository.update(accountKey, Account.IS_VALIDATED, Boolean.TRUE);
    }
  }
}
