package com.budgetview.triggers.projects;

import com.budgetview.model.Project;
import com.budgetview.model.ProjectItem;
import com.budgetview.model.ProjectTransfer;
import com.budgetview.model.Series;
import com.budgetview.shared.model.BudgetArea;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class ProjectItemToSeriesTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(ProjectItem.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Glob project = repository.find(Key.create(Project.TYPE, values.get(ProjectItem.PROJECT)));
        boolean active = project.isTrue(Project.ACTIVE) && values.isTrue(ProjectItem.ACTIVE);
        if (active && ProjectItem.usesExtrasSeries(values) && values.get(ProjectItem.SERIES) == null) {
          createSeries(key, values, repository);
        }
      }

      public void visitUpdate(Key itemKey, FieldValuesWithPrevious values) throws Exception {
        Glob item = repository.get(itemKey);
        if (!ProjectItem.usesExtrasSeries(item)) {
          return;
        }
        if (values.contains(ProjectItem.ACCOUNT)) {
          Glob series = repository.findLinkTarget(repository.get(itemKey), ProjectItem.SERIES);
          if (series != null) {
            repository.update(series.getKey(), value(Series.TARGET_ACCOUNT, values.get(ProjectItem.ACCOUNT)));
          }
        }
        if (values.contains(ProjectItem.ACTIVE)) {
          updateActiveStateForSeries(item, repository);
          return;
        }
        if (values.contains(ProjectItem.LABEL)) {
          Glob series = repository.findLinkTarget(repository.get(itemKey), ProjectItem.SERIES);
          if (series != null) {
            repository.update(series.getKey(),
                              value(Series.NAME, values.get(ProjectItem.LABEL)));
          }
        }
      }

      public void visitDeletion(Key itemKey, FieldValues previousValues) throws Exception {
        Integer seriesId = previousValues.get(ProjectItem.SERIES);
        if (seriesId == null) {
          return;
        }
        Key seriesKey = Key.create(Series.TYPE, seriesId);
        if (repository.contains(seriesKey)) {
          repository.delete(seriesKey);
        }
      }
    });
    changeSet.safeVisit(Project.TYPE, new DefaultChangeSetVisitor() {
      public void visitUpdate(Key projectKey, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Project.ACTIVE)) {
          for (Glob item : repository.getAll(ProjectItem.TYPE, linkedTo(projectKey, ProjectItem.PROJECT))) {
            updateActiveStateForSeries(item, repository);
          }
        }
      }
    });
  }

  public void updateActiveStateForSeries(Glob item, GlobRepository repository) {
    Glob series = repository.findLinkTarget(item, ProjectItem.SERIES);
    if (series == null) {
      createSeries(item.getKey(), item, repository);
      return;
    }

    Glob project = repository.findLinkTarget(item, ProjectItem.PROJECT);
    boolean active = project.isTrue(Project.ACTIVE) && item.isTrue(ProjectItem.ACTIVE);
    repository.update(series.getKey(), Series.ACTIVE, active);

    Integer mirrorId = series.get(Series.MIRROR_SERIES);
    if (mirrorId != null) {
      repository.update(Key.create(Series.TYPE, mirrorId), Series.ACTIVE, active);
    }
  }

  public static Glob createSeries(Glob projectItem, GlobRepository repository) {
    return createSeries(projectItem.getKey(), projectItem, repository);
  }

  public static Glob createSeries(Key projectItemKey, FieldValues projectItemValues, GlobRepository repository) {
    if (!ProjectItem.usesExtrasSeries(projectItemValues)) {
      return null;
    }
    Integer projectId = projectItemValues.get(ProjectItem.PROJECT);
    Glob project = repository.get(Key.create(Project.TYPE, projectId));
    Integer firstMonth = projectItemValues.get(ProjectItem.FIRST_MONTH);
    Integer lastMonth = ProjectItem.getLastMonth(projectItemValues);
    Glob series =
      repository.create(Series.TYPE,
                        value(Series.GROUP, project.get(Project.SERIES_GROUP)),
                        value(Series.BUDGET_AREA, BudgetArea.EXTRAS.getId()),
                        value(Series.NAME, projectItemValues.get(ProjectItem.LABEL)),
                        value(Series.ACTIVE, projectItemValues.get(ProjectItem.ACTIVE)),
                        value(Series.TARGET_ACCOUNT, projectItemValues.get(ProjectItem.ACCOUNT)),
                        value(Series.IS_AUTOMATIC, false),
                        value(Series.FIRST_MONTH, firstMonth),
                        value(Series.LAST_MONTH, lastMonth));
    repository.update(projectItemKey, ProjectItem.SERIES, series.get(Series.ID));
    return series;
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(ProjectItem.TYPE)) {
      createMissingSeries(repository);
    }
  }

  public static void createMissingSeries(GlobRepository repository) {
    for (Glob item : repository.getAll(ProjectItem.TYPE)) {
      if (item.get(ProjectItem.SERIES) == null) {
        if (ProjectItem.usesExtrasSeries(item)) {
          createSeries(item.getKey(), item, repository);
        }
        else {
          Glob transfer = ProjectTransfer.getTransferFromItem(item, repository);
          ProjectTransferToSeriesTrigger.createSavingsSeriesIfComplete(transfer.getKey(), repository);
        }
      }
    }
  }
}
