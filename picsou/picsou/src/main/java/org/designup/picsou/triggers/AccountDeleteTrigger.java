package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
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
    Set<Key> keySet = changeSet.getDeleted(Account.TYPE);
    for (Key key : keySet) {
      repository.delete(Transaction.TYPE, GlobMatchers.linkedTo(key, Transaction.ACCOUNT));
      repository.delete(Series.TYPE, or(linkedTo(key, Series.FROM_ACCOUNT),
                                        linkedTo(key, Series.TO_ACCOUNT)));
    }
    Set<Key> created = changeSet.getCreated(Account.TYPE);
    for (Key key : created) {
      repository.update(key, Account.IS_VALIDADED, Boolean.TRUE);
    }
  }
}
