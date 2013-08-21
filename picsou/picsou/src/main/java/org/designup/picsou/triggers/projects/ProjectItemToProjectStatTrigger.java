package org.designup.picsou.triggers.projects;

import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.ProjectItem;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class ProjectItemToProjectStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    final Set<Integer> projectIds = new HashSet<Integer>();
    changeSet.safeVisit(ProjectItem.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        projectIds.add(values.get(ProjectItem.PROJECT));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        projectIds.add(repository.get(key).get(ProjectItem.PROJECT));
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        projectIds.add(previousValues.get(ProjectItem.PROJECT));
      }
    });
    for (Integer projectId : projectIds) {
      Glob project = repository.find(Key.create(Project.TYPE, projectId));
      if (project != null) {
        updateProject(project, repository);
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Project.TYPE) || changedTypes.contains(ProjectItem.TYPE)) {
      for (Glob project : repository.getAll(Project.TYPE)) {
        updateProject(project, repository);
      }
    }
  }

  private void updateProject(Glob project, GlobRepository repository) {

    GlobList items = repository.findLinkedTo(project, ProjectItem.PROJECT);

    double totalPlanned = 0.00;
    for (Glob item : items) {
      totalPlanned += item.get(ProjectItem.PLANNED_AMOUNT);
    }
    Key projectStatKey = Key.create(ProjectStat.TYPE, project.get(Project.ID));
    repository.update(projectStatKey, ProjectStat.PLANNED_AMOUNT, totalPlanned);

    Integer firstMonth = null;
    Integer lastMonth = null;
    for (Glob item : items) {
      Integer month = item.get(ProjectItem.MONTH);
      if (month == null) {
        continue;
      }
      if ((firstMonth == null) || (lastMonth == null)) {
        firstMonth = month;
        lastMonth = month;
        continue;
      }
      if (month < firstMonth) {
        firstMonth = month;
      }
      if (month > lastMonth) {
        lastMonth = month;
      }
    }
    repository.update(projectStatKey,
                      value(ProjectStat.FIRST_MONTH, firstMonth),
                      value(ProjectStat.LAST_MONTH, lastMonth));
  }
}
