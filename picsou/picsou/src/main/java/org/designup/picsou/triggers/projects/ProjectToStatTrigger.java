package org.designup.picsou.triggers.projects;

import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.model.Project;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class ProjectToStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Project.TYPE, new DefaultChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        repository.create(ProjectStat.TYPE,
                          value(ProjectStat.PROJECT, key.get(Project.ID)));
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Key projectStatKey = Key.create(ProjectStat.TYPE, previousValues.get(Project.ID));
        repository.delete(projectStatKey);
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Project.TYPE)) {
      repository.deleteAll(ProjectStat.TYPE);
      for (Glob project : repository.getAll(Project.TYPE)) {
        repository.create(ProjectStat.TYPE,
                          value(ProjectStat.PROJECT, project.get(Project.ID)));
      }
    }
  }
}
