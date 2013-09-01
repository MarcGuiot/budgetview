package org.designup.picsou.triggers.projects;

import org.designup.picsou.model.Account;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;

import java.util.Set;

public class ProjectTransferAccountChangeTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (!changeSet.containsChanges(Account.TYPE)) {
      return;
    }

  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
