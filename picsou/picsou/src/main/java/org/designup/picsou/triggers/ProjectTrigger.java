package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class ProjectTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    changeSet.safeVisit(Project.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Glob series = repository.create(Series.TYPE,
                                        value(Series.BUDGET_AREA, BudgetArea.EXTRAS.getId()),
                                        value(Series.NAME, values.get(Project.NAME)),
                                        value(Series.IS_AUTOMATIC, false));
        repository.update(key, value(Project.SERIES, series.get(Series.ID)));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob project = repository.get(key);
        if (values.contains(Project.NAME)) {
          repository.update(Key.create(Series.TYPE, project.get(Project.SERIES)),
                            value(Series.NAME, values.get(Project.NAME)));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Key seriesKey = Key.create(Series.TYPE, previousValues.get(Project.SERIES));
        repository.delete(seriesKey);
        repository.delete(ProjectItem.TYPE, GlobMatchers.linkedTo(key, ProjectItem.PROJECT));
        repository.delete(SeriesBudget.TYPE, GlobMatchers.linkedTo(seriesKey, SeriesBudget.SERIES));
      }
    });

    changeSet.safeVisit(ProjectItem.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        updateProjectAmounts(key, values, repository, false);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(ProjectItem.AMOUNT)) {
          updateProjectAmounts(key, values, repository, false);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        updateProjectAmounts(key, previousValues, repository, true);
      }
    });
  }

  private void updateProjectAmounts(Key projectItemKey,
                                    FieldValues projectItemValues,
                                    GlobRepository repository,
                                    boolean deleted) {
    Integer projectId = null;
    if (projectItemValues.contains(ProjectItem.PROJECT)) {
      projectId = projectItemValues.get(ProjectItem.PROJECT);
    }
    else {
      Glob projectItem = repository.find(projectItemKey);
      if (projectItem != null) {
        projectId = projectItem.get(ProjectItem.PROJECT);
      }
    }
    Glob project = repository.find(Key.create(Project.TYPE, projectId));
    if (project == null) {
      return;
    }

    double newValue = project.get(Project.TOTAL_AMOUNT) +
                      projectItemValues.get(ProjectItem.AMOUNT) * (deleted ? -1 : +1);
    repository.update(project.getKey(), value(Project.TOTAL_AMOUNT, newValue));

    int monthId = getMonthId(projectItemKey, projectItemValues, repository);
    GlobList seriesBudgetList = SeriesBudget.getAll(project.get(Project.SERIES), monthId, repository);
    if (seriesBudgetList.isEmpty() && !deleted) {
      repository.create(SeriesBudget.TYPE,
                        value(SeriesBudget.SERIES, project.get(Project.SERIES)),
                        value(SeriesBudget.MONTH, monthId),
                        value(SeriesBudget.AMOUNT, projectItemValues.get(ProjectItem.AMOUNT)),
                        value(SeriesBudget.ACTIVE, true));
    }
    else if (!seriesBudgetList.isEmpty()) {
      Glob seriesBudget = seriesBudgetList.getFirst();
      double seriesBudgetValue = seriesBudget.get(SeriesBudget.AMOUNT) +
                                 projectItemValues.get(ProjectItem.AMOUNT) * (deleted ? -1 : +1);
      repository.update(seriesBudget.getKey(),
                        value(SeriesBudget.AMOUNT, seriesBudgetValue),
                        value(SeriesBudget.ACTIVE, true));
    }
  }

  private int getMonthId(Key projectItemKey, FieldValues projectItemValues, GlobRepository repository) {
    if ((projectItemValues.contains(ProjectItem.MONTH))) {
      return projectItemValues.get(ProjectItem.MONTH);
    }

    return repository.get(projectItemKey).get(ProjectItem.MONTH);
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
