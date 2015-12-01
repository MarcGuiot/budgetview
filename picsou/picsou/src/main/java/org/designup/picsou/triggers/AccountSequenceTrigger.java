package org.designup.picsou.triggers;

import org.designup.picsou.gui.accounts.utils.AutomaticAccountComparator;
import org.designup.picsou.model.Account;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.ChangeVisitor;

import java.util.Set;

public class AccountSequenceTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(Account.TYPE)) {
      changeSet.safeVisit(Account.TYPE, new ChangeVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
          Integer max = repository.getAll(Account.TYPE).getMaxValue(Account.SEQUENCE);
          if (max == null) {
            max = 0;
          }
          repository.update(key, Account.SEQUENCE, max + 1);
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {

        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {

        }

        public void complete() {

        }
      });
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }

  public static void resetSequence(GlobRepository repository) {
    int sequence = 0;
    for (Glob account : repository.getAll(Account.TYPE).sortSelf(new AutomaticAccountComparator())) {
      repository.update(account.getKey(), Account.SEQUENCE, sequence++);
    }
  }
}
