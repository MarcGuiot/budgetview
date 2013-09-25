package org.designup.picsou.triggers.projects;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.ClosedMonthRange;
import org.globsframework.model.*;
import org.globsframework.utils.Utils;

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
    changeSet.safeVisit(ProjectItemAmount.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Glob projectItem = repository.find(Key.create(ProjectItem.TYPE, values.get(ProjectItemAmount.PROJECT_ITEM)));
        if (projectItem != null) {
          projectIds.add(projectItem.get(ProjectItem.PROJECT));
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob projectItemAmount = repository.get(key);
        Glob projectItem = repository.find(Key.create(ProjectItem.TYPE, projectItemAmount.get(ProjectItemAmount.PROJECT_ITEM)));
        if (projectItem != null) {
          projectIds.add(projectItem.get(ProjectItem.PROJECT));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Glob projectItem = repository.find(Key.create(ProjectItem.TYPE, previousValues.get(ProjectItemAmount.PROJECT_ITEM)));
        if (projectItem != null) {
          projectIds.add(projectItem.get(ProjectItem.PROJECT));
        }
      }
    });
    return projectIds;
  }

  private void updateProject(Integer projectId, GlobRepository repository) {
    Glob project = repository.find(Key.create(Project.TYPE, projectId));
    if (project == null) {
      return;
    }

    Set<Integer> seriesIds =
      repository.getAll(ProjectItem.TYPE, linkedTo(project, ProjectItem.PROJECT))
        .getValueSet(ProjectItem.SERIES);
    for (Integer seriesId : seriesIds) {
      updateSeries(project, seriesId, repository);
    }
  }

  private void updateSeries(Glob project, Integer seriesId, GlobRepository repository) {
    Key seriesKey = Key.create(Series.TYPE, seriesId);

    // Gather months for the ProjectItems and Transactions associated to this project
    GlobList projectItems = repository.getAll(ProjectItem.TYPE, linkedTo(seriesKey, ProjectItem.SERIES));
    SortedSet<Integer> months = new TreeSet<Integer>();
    for (Glob item : projectItems) {
      Integer firstItemMonth = item.get(ProjectItem.FIRST_MONTH);
      months.add(firstItemMonth);
      Integer lastItemMonth = ProjectItem.getLastMonth(item);
      if (!Utils.equal(firstItemMonth, lastItemMonth)) {
        months.add(lastItemMonth);
      }
    }
    GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId).getGlobs();
    months.addAll(transactions.getValueSet(Transaction.BUDGET_MONTH));
    if (months.isEmpty()) {
      repository.update(seriesKey, Series.FIRST_MONTH, null);
      repository.update(seriesKey, Series.LAST_MONTH, null);
      return;
    }

    // Create missing SeriesBudget and set Series range
    ClosedMonthRange range = new ClosedMonthRange(months.first(), months.last());
    for (Integer monthId : range.asList()) {
      SeriesBudget.findOrCreate(seriesId, monthId, repository);
    }
    repository.update(seriesKey, Series.FIRST_MONTH, range.getMin());
    repository.update(seriesKey, Series.LAST_MONTH, range.getMax());

    // Reset all SeriesBudget planned amount to 0.00 and compute total actual
    for (Glob seriesBudget : repository.getAll(SeriesBudget.TYPE, linkedTo(seriesKey, SeriesBudget.SERIES))) {
      Double actual = seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT, 0.00);
      boolean hasActual = Amounts.isNotZero(actual);
      if (range.contains(seriesBudget.get(SeriesBudget.MONTH))) {
        repository.update(seriesBudget.getKey(),
                          value(SeriesBudget.PLANNED_AMOUNT, 0.00),
                          value(SeriesBudget.ACTIVE, hasActual));
      }
      else if (!hasActual) {
        repository.delete(seriesBudget);
      }
      else {
        repository.update(seriesBudget.getKey(),
                          value(SeriesBudget.PLANNED_AMOUNT, 0.00),
                          value(SeriesBudget.ACTIVE, hasActual));
      }
    }

    // Set all SeriesBudget planned amounts according to the active items
    if (project.isTrue(Project.ACTIVE)) {
      for (Glob item : projectItems) {
        if (item.isTrue(ProjectItem.ACTIVE)) {
          Integer firstMonthId = item.get(ProjectItem.FIRST_MONTH);
          Integer lastMonthId = ProjectItem.getLastMonth(item);
          for (int monthId = firstMonthId; monthId <= lastMonthId; monthId = Month.next(monthId)) {
            Glob seriesBudget = SeriesBudget.find(seriesId, monthId, repository);
            double planned = seriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0) + getPlanned(item, monthId, repository);
            repository.update(seriesBudget.getKey(),
                              value(SeriesBudget.PLANNED_AMOUNT, planned),
                              value(SeriesBudget.ACTIVE, true));
          }
        }
      }
    }
  }

  private Double getPlanned(Glob item, int monthId, GlobRepository repository) {
    if (ProjectItemType.TRANSFER.equals(ProjectItemType.get(item))) {
      Glob transfer = ProjectTransfer.getTransferFromItem(item, repository);
      if (!ProjectTransfer.isSavings(transfer, repository)) {
        return 0.00;
      }
    }
    return ProjectItem.getAmount(item, monthId, repository);
  }
}
