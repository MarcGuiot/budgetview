package com.budgetview.triggers.projects;

import com.budgetview.model.ProjectItem;
import com.budgetview.model.ProjectTransfer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.Set;

public class ProjectTransferDeletionTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(ProjectItem.TYPE,  new DefaultChangeSetVisitor() {
      public void visitDeletion(Key projectItemKey, FieldValues values) throws Exception {
        Key projectTransferKey = Key.create(ProjectTransfer.TYPE, projectItemKey.get(ProjectItem.ID));
        Glob transfer = repository.find(projectTransferKey);
        if (transfer != null) {
          repository.delete(transfer);
        }
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(ProjectTransfer.TYPE)) {
      for (Glob transfer : repository.getAll(ProjectTransfer.TYPE)) {
        if (repository.findLinkTarget(transfer, ProjectTransfer.PROJECT_ITEM) == null) {
          repository.delete(transfer);
        }
      }
    }
  }
}
