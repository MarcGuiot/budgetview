package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.DefaultChangeSetListener;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;

public class AccountDeletionTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsCreations(Account.TYPE)) {
      for (Key accountKey : changeSet.getCreated(Account.TYPE)) {
        repository.update(accountKey, Account.IS_VALIDATED, Boolean.TRUE);
      }
    }
    if (changeSet.containsDeletions(Account.TYPE)) {
      processAccountDeletions(changeSet.getDeleted(Account.TYPE), repository);
    }
  }

  public void processAccountDeletions(Set<Key> deletedKeys, GlobRepository repository) {

    Set<Integer> projectIds = new HashSet<Integer>();
    Set<Glob> itemsToDelete = new HashSet<Glob>();
    Set<Glob> seriesToDelete = new HashSet<Glob>();
    for (Key accountKey : deletedKeys) {
      itemsToDelete.addAll(repository.findLinkedTo(accountKey, ProjectItem.ACCOUNT));
      for (Glob transfer : repository.getAll(ProjectTransfer.TYPE,
                                             or(linkedTo(accountKey, ProjectTransfer.FROM_ACCOUNT),
                                                linkedTo(accountKey, ProjectTransfer.TO_ACCOUNT))
      )) {
        itemsToDelete.add(repository.findLinkTarget(transfer, ProjectTransfer.PROJECT_ITEM));
      }
      repository.delete(Transaction.TYPE, linkedTo(accountKey, Transaction.ACCOUNT));
      seriesToDelete.addAll(repository.getAll(Series.TYPE, or(linkedTo(accountKey, Series.TARGET_ACCOUNT),
                                                              linkedTo(accountKey, Series.FROM_ACCOUNT),
                                                              linkedTo(accountKey, Series.TO_ACCOUNT))));
      repository.delete(AccountPositionError.TYPE, linkedTo(accountKey, AccountPositionError.ACCOUNT));
    }

    for (Glob item : itemsToDelete) {
      projectIds.add(item.get(ProjectItem.PROJECT));
    }

    for (Glob item : itemsToDelete) {
      ProjectItem.deleteAll(item, repository);
    }

    for (Glob series : seriesToDelete) {
      if (repository.contains(series.getKey())) {
        Series.delete(series, repository);
      }
    }

    Integer defaultMainAccountId = Account.getDefaultMainAccountId(repository);
    Key defaultAccountKey = defaultMainAccountId != null ? Key.create(Account.TYPE, defaultMainAccountId) : null;
    for (Integer projectId : projectIds) {
      if (!repository.contains(ProjectItem.TYPE, fieldEquals(ProjectItem.PROJECT, projectId))) {
        Project.deleteAll(Key.create(Project.TYPE, projectId), repository);
      }
      else {
        Glob project = repository.get(Key.create(Project.TYPE, projectId));
        if (deletedKeys.contains(Key.create(Account.TYPE, project.get(Project.DEFAULT_ACCOUNT)))) {
          repository.setTarget(project.getKey(), Project.DEFAULT_ACCOUNT, defaultAccountKey);
        }
      }
    }
  }
}
