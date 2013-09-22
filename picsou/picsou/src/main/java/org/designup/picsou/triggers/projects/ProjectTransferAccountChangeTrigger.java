package org.designup.picsou.triggers.projects;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.ProjectItemAmount;
import org.designup.picsou.model.ProjectTransfer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobUtils;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;

public class ProjectTransferAccountChangeTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (!changeSet.containsChanges(Account.TYPE)) {
      return;
    }

    Set<Integer> accountIds = new HashSet<Integer>();
    accountIds.addAll(GlobUtils.getValues(changeSet.getDeleted(Account.TYPE), Account.ID));
    accountIds.addAll(GlobUtils.getValues(changeSet.getUpdated(Account.ACCOUNT_TYPE), Account.ID));
    for (Glob transfer : repository.getAll(ProjectTransfer.TYPE,
                                           or(fieldIn(ProjectTransfer.FROM_ACCOUNT, accountIds),
                                              fieldIn(ProjectTransfer.TO_ACCOUNT, accountIds)))) {
      update(transfer, repository);
    }
  }

  private void update(Glob transfer, GlobRepository repository) {
    Key fromAccountKey = Key.create(Account.TYPE, transfer.get(ProjectTransfer.FROM_ACCOUNT));
    Key toAccountKey = Key.create(Account.TYPE, transfer.get(ProjectTransfer.TO_ACCOUNT));

    if (!repository.contains(fromAccountKey) || !repository.contains(toAccountKey)) {
      deleteTransfer(transfer, repository);
      return;
    }

    if (!Account.isSavings(repository.get(fromAccountKey)) &&
        !Account.isSavings(repository.get(toAccountKey))) {
      deleteTransfer(transfer, repository);
      return;
    }
  }

  private void deleteTransfer(Glob transfer, GlobRepository repository) {
    repository.delete(Key.create(ProjectItem.TYPE, transfer.get(ProjectTransfer.PROJECT_ITEM)));
    repository.delete(transfer);
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
