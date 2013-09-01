package org.designup.picsou.triggers.projects;

import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.ProjectItemType;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

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
    Key projectStatKey = Key.create(ProjectStat.TYPE, project.get(Project.ID));
    updatePlannedAmount(project, projectStatKey, repository);
    updateFirstAndLastMonth(project, projectStatKey, repository);
  }

  private void updatePlannedAmount(Glob project, Key projectStatKey, GlobRepository repository) {
    double totalPlanned = 0.00;
    for (Glob item : repository.getAll(ProjectItem.TYPE,
                                       and(linkedTo(project, ProjectItem.PROJECT),
                                           not(fieldEquals(ProjectItem.ITEM_TYPE, ProjectItemType.TRANSFER.getId()))))) {
      totalPlanned += ProjectItem.getTotalPlannedAmount(item);
    }
    repository.update(projectStatKey, ProjectStat.PLANNED_AMOUNT, totalPlanned);
  }

  private void updateFirstAndLastMonth(Glob project, Key projectStatKey, GlobRepository repository) {
    Integer firstMonth = null;
    Integer lastMonth = null;
    for (Glob item : repository.findLinkedTo(project, ProjectItem.PROJECT)) {
      Integer firstItemMonth = item.get(ProjectItem.MONTH);
      if (firstItemMonth == null) {
        continue;
      }
      Integer lastItemMonth = ProjectItem.getLastMonth(item);
      if ((firstMonth == null) || (lastMonth == null)) {
        firstMonth = firstItemMonth;
        lastMonth = lastItemMonth;
        continue;
      }
      if (firstItemMonth < firstMonth) {
        firstMonth = firstItemMonth;
      }
      if (lastItemMonth > lastMonth) {
        lastMonth = lastItemMonth;
      }
    }
    repository.update(projectStatKey,
                      value(ProjectStat.FIRST_MONTH, firstMonth),
                      value(ProjectStat.LAST_MONTH, lastMonth));
  }
}
