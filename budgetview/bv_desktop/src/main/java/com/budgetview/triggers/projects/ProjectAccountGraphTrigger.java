package com.budgetview.triggers.projects;

import com.budgetview.model.Account;
import com.budgetview.model.ProjectAccountGraph;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.Set;

public class ProjectAccountGraphTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Account.TYPE, new DefaultChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        repository.findOrCreate(Key.create(ProjectAccountGraph.TYPE, key.get(Account.ID)));
      }

      public void visitDeletion(Key key, FieldValues values) throws Exception {
        repository.delete(Key.create(ProjectAccountGraph.TYPE, key.get(Account.ID)));
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      if (changedTypes.contains(Account.TYPE)) {
        Set<Integer> accountIds = repository.getAll(Account.TYPE).getValueSet(Account.ID);
        accountIds.removeAll(repository.getAll(ProjectAccountGraph.TYPE).getValueSet(ProjectAccountGraph.ACCOUNT));
        for (Integer accountId : accountIds) {
          repository.findOrCreate(Key.create(ProjectAccountGraph.TYPE, accountId));
        }
      }
  }
}
