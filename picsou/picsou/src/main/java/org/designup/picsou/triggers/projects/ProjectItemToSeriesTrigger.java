package org.designup.picsou.triggers.projects;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

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
        if (values.contains(ProjectItem.ACTIVE)) {
          createOrDeleteSeriesForItem(item, repository);
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
            createOrDeleteSeriesForItem(item, repository);
          }
        }
      }
    });
  }

  public static void createOrDeleteSeriesForItem(Glob item, GlobRepository repository) {
    Glob project = repository.findLinkTarget(item, ProjectItem.PROJECT);
    boolean active = project.isTrue(Project.ACTIVE) && item.isTrue(ProjectItem.ACTIVE);
    if (active && item.get(ProjectItem.SERIES) == null) {
      createSeries(item.getKey(), item, repository);
    }
    else if (!active && item.get(ProjectItem.SERIES) != null) {
      Integer seriesId = item.get(ProjectItem.SERIES);
      Key seriesKey = Key.create(Series.TYPE, seriesId);
      if (repository.contains(seriesKey) &&
          !repository.contains(Transaction.TYPE,
                               and(fieldEquals(Transaction.SERIES, seriesId),
                                   isFalse(Transaction.PLANNED)))) {
        Glob series = repository.get(seriesKey);
        if (series.get(Series.MIRROR_SERIES) != null){
          if (!repository.contains(Transaction.TYPE,
                              and(fieldEquals(Transaction.SERIES, seriesId),
                                  isFalse(Transaction.PLANNED)))) {
            repository.update(item.getKey(), ProjectItem.SERIES, null);
            repository.delete(seriesKey);
            repository.delete(KeyBuilder.newKey(Series.TYPE, series.get(Series.ID)));
          }
        }
        else {
          repository.update(item.getKey(), ProjectItem.SERIES, null);
          repository.delete(seriesKey);
        }
      }
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
                        value(Series.TARGET_ACCOUNT, projectItemValues.get(ProjectItem.ACCOUNT)),
                        value(Series.IS_AUTOMATIC, false),
                        value(Series.FIRST_MONTH, firstMonth),
                        value(Series.LAST_MONTH, lastMonth));
    repository.update(projectItemKey, ProjectItem.SERIES, series.get(Series.ID));
    return series;
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(ProjectItem.TYPE)) {
      for (Glob item : repository.getAll(ProjectItem.TYPE)) {
        if (item.isTrue(ProjectItem.ACTIVE) && item.get(ProjectItem.SERIES) == null) {
          createSeries(item.getKey(), item, repository);
        }
      }
    }
  }
}
