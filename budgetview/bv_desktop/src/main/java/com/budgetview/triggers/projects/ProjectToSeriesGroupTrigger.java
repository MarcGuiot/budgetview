package com.budgetview.triggers.projects;

import com.budgetview.model.*;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.triggers.DeleteUnusedSeriesGroupTrigger;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.utils.Utils;

import java.util.Set;

import static com.budgetview.model.ProjectItemType.isExpenses;
import static org.globsframework.model.FieldValue.value;

public class ProjectToSeriesGroupTrigger extends AbstractChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(ProjectItem.TYPE, new DefaultChangeSetVisitor() {
      public void visitCreation(Key itemKey, FieldValues values) throws Exception {

        Glob item = repository.find(itemKey);
        if (item.get(ProjectItem.SERIES) == null) {
          return;
        }

        if (ProjectItem.usesExtrasSeries(item)) {
          Glob project = repository.get(Key.create(Project.TYPE, values.get(ProjectItem.PROJECT)));
          Glob group = repository.findLinkTarget(project, Project.SERIES_GROUP);
          if (group == null) {
            group = createGroup(project, repository);
          }

          Glob series = repository.findLinkTarget(item, ProjectItem.SERIES);
          repository.update(series.getKey(), Series.GROUP, group.get(SeriesGroup.ID));
        }
      }
    });

    changeSet.safeVisit(Project.TYPE, new DefaultChangeSetVisitor() {
      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Project.NAME)) {
          Glob project = repository.get(key);
          Key groupKey = project.getTargetKey(Project.SERIES_GROUP);
          if (repository.contains(groupKey)) {
            repository.update(groupKey, SeriesGroup.NAME, values.get(Project.NAME));
          }
        }
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Project.TYPE) || changedTypes.contains(ProjectItem.TYPE)) {
      createGroupsForProjects(repository);
    }
  }

  public static void createGroupsForProjects(GlobRepository repository) {
    for (Glob project : repository.getAll(ProjectItem.TYPE, isExpenses()).getTargets(ProjectItem.PROJECT, repository)) {
      if (repository.findLinkTarget(project, Project.SERIES_GROUP) == null) {
        createGroup(project, repository);
      }
    }
  }

  public static Glob createGroup(Glob project, GlobRepository repository) {
    Glob group = repository.create(SeriesGroup.TYPE,
                                   value(SeriesGroup.BUDGET_AREA, BudgetArea.EXTRAS.getId()),
                                   value(SeriesGroup.NAME, project.get(Project.NAME)),
                                   value(SeriesGroup.EXPANDED, false));
    repository.update(project.getKey(), Project.SERIES_GROUP, group.get(SeriesGroup.ID));

    Integer newGroupId = group.get(SeriesGroup.ID);
    for (Glob item : repository.findLinkedTo(project, ProjectItem.PROJECT)) {
      Glob series = repository.findLinkTarget(item, ProjectItem.SERIES);
      if (series != null && ProjectItem.usesExtrasSeries(item)) {
        Integer previousGroupId = series.get(Series.GROUP);
        if (!Utils.equal(newGroupId, previousGroupId)) {
          repository.update(series.getKey(), Series.GROUP, group.get(SeriesGroup.ID));
          DeleteUnusedSeriesGroupTrigger.deleteGroupIfNeeded(previousGroupId, repository);
        }
      }
    }

    return group;
  }
}
