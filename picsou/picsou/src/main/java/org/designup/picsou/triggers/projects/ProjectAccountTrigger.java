package org.designup.picsou.triggers.projects;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Project;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.Set;

public class ProjectAccountTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsCreationsOrDeletions(Project.TYPE)) {
      final Integer defaultMainAccountId = Account.getDefaultMainAccountId(repository);
      changeSet.safeVisit(Project.TYPE, new DefaultChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
          if (values.get(Project.DEFAULT_ACCOUNT) == null) {
            repository.update(key, Project.DEFAULT_ACCOUNT, defaultMainAccountId);
          }
        }
      });
    }
    if (changeSet.containsDeletions(Account.TYPE)) {

    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {

  }
}
