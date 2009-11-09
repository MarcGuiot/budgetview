package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.Series;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.or;
import static org.globsframework.model.utils.GlobMatchers.linkedTo;

import java.util.Set;

public class AccountDeleteTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (!changeSet.containsCreationsOrDeletions(Account.TYPE)) {
      return;
    }
    Set<Key> keySet = changeSet.getDeleted(Account.TYPE);
    for (Key key : keySet) {
      repository.delete(repository.getAll(Transaction.TYPE, GlobMatchers.linkedTo(key, Transaction.ACCOUNT)));
      repository.delete(repository.getAll(Series.TYPE, 
                                          or(linkedTo(key, Series.FROM_ACCOUNT),
                                             linkedTo(key, Series.TO_ACCOUNT))));
    }
  }
}
