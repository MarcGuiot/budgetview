package org.designup.picsou.triggers.projects;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.util.ClosedMonthRange;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobPrinter;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class ProjectItemToSeriesBudgetTrigger extends AbstractChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    final Set<Integer> projectIds = getProjectsForChangedItems(changeSet, repository);
    for (Integer projectId : projectIds) {
      updateProject(projectId, repository);
    }
  }

  private Set<Integer> getProjectsForChangedItems(ChangeSet changeSet, final GlobRepository repository) {
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
    for (Key projectKey : changeSet.getUpdated(Project.ACTIVE)) {
      projectIds.add(projectKey.get(Project.ID));
    }
    return projectIds;
  }

  private void updateProject(Integer projectId, GlobRepository repository) {
    Glob project = repository.find(Key.create(Project.TYPE, projectId));
    if (project == null) {
      return;
    }

    Integer seriesId = project.get(Project.SERIES);
    Key seriesKey = project.getTargetKey(Project.SERIES);

    GlobList projectItems = repository.getAll(ProjectItem.TYPE, linkedTo(project, ProjectItem.PROJECT));
    SortedSet<Integer> months = new TreeSet<Integer>();
    months.addAll(projectItems.getValueSet(ProjectItem.MONTH));
    if (months.isEmpty()) {
      repository.update(seriesKey, Series.FIRST_MONTH, null);
      repository.update(seriesKey, Series.LAST_MONTH, null);
      for (Glob seriesBudget : repository.getAll(SeriesBudget.TYPE, linkedTo(seriesKey, SeriesBudget.SERIES))) {
        Double actualAmount = seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT, 0.00);
        repository.update(seriesBudget.getKey(),
                          value(SeriesBudget.PLANNED_AMOUNT, 0.00),
                          value(SeriesBudget.ACTIVE, Amounts.isNotZero(actualAmount)));
      }
      return;
    }

    ClosedMonthRange range = new ClosedMonthRange(months.first(), months.last());

    // 1. Create all SeriesBudget
    for (Integer monthId : range.asList()) {
      SeriesBudget.findOrCreate(seriesId, monthId, repository);
    }

    // 2. Reset all SeriesBudget planned amount to 0.00
    for (Glob seriesBudget : repository.getAll(SeriesBudget.TYPE, linkedTo(seriesKey, SeriesBudget.SERIES))) {
      boolean hasActual = Amounts.isNotZero(seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT, 0.00));
      if (range.contains(seriesBudget.get(SeriesBudget.MONTH))) {
        repository.update(seriesBudget.getKey(),
                          value(SeriesBudget.PLANNED_AMOUNT, 0.00),
                          value(SeriesBudget.ACTIVE, hasActual));
      }
      else if (!hasActual) {
        repository.delete(seriesBudget.getKey());
      }
      else {
        repository.update(seriesBudget.getKey(),
                          value(SeriesBudget.PLANNED_AMOUNT, 0.00),
                          value(SeriesBudget.ACTIVE, hasActual));
      }
    }

    // 3. Set all SeriesBudget planned amounts according to the active items
    if (project.isTrue(Project.ACTIVE)) {
      for (Glob item : projectItems) {
        if (item.isTrue(ProjectItem.ACTIVE)) {
          Integer monthId = item.get(ProjectItem.MONTH);
          Glob seriesBudget = SeriesBudget.find(seriesId, monthId, repository);
          double planned = seriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0) + item.get(ProjectItem.PLANNED_AMOUNT, 0.0);
          repository.update(seriesBudget.getKey(),
                            value(SeriesBudget.PLANNED_AMOUNT, planned),
                            value(SeriesBudget.ACTIVE, true));
        }
      }
    }

    repository.update(seriesKey, Series.FIRST_MONTH, range.getMin());
    repository.update(seriesKey, Series.LAST_MONTH, range.getMax());
  }
}
