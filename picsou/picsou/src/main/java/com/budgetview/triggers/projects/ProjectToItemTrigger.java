package com.budgetview.triggers.projects;

import com.budgetview.model.Project;
import com.budgetview.model.ProjectItem;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class ProjectToItemTrigger extends AbstractChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Project.TYPE, new DefaultChangeSetVisitor() {
      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        repository.delete(ProjectItem.TYPE, linkedTo(key, ProjectItem.PROJECT));
      }
    });
  }
}
