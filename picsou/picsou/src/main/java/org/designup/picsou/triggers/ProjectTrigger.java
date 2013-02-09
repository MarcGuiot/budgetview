package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.model.util.ClosedMonthRange;
import org.globsframework.model.*;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class ProjectTrigger extends AbstractChangeSetListener{

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
          repository.update(project.getTargetKey(Project.SERIES),
                            value(Series.NAME, values.get(Project.NAME)));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Key seriesKey = Key.create(Series.TYPE, previousValues.get(Project.SERIES));
        repository.delete(seriesKey);
        repository.delete(ProjectItem.TYPE, linkedTo(key, ProjectItem.PROJECT));
        repository.delete(SeriesBudget.TYPE, linkedTo(seriesKey, SeriesBudget.SERIES));
      }
    });

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
    return projectIds;
  }

  private void updateProject(Integer projectId, GlobRepository repository) {
    Glob project = repository.get(Key.create(Project.TYPE, projectId));
    if (project == null) {
      return;
    }

    Integer seriesId = project.get(Project.SERIES);
    Key seriesKey = project.getTargetKey(Project.SERIES);
    for (Glob seriesBudget : repository.getAll(SeriesBudget.TYPE, linkedTo(seriesKey, SeriesBudget.SERIES))) {
      boolean hasTransactions = Amounts.isNotZero(seriesBudget.get(SeriesBudget.OBSERVED_AMOUNT));
      repository.update(seriesBudget.getKey(),
                        value(SeriesBudget.PLANNED_AMOUNT, 0.00),
                        value(SeriesBudget.ACTIVE, hasTransactions));
    }


    GlobList projectItems = repository.getAll(ProjectItem.TYPE, linkedTo(project, ProjectItem.PROJECT));
    SortedSet<Integer> months = new TreeSet<Integer>();
    months.addAll(projectItems.getValueSet(ProjectItem.MONTH));
    if (months.isEmpty()) {
      repository.update(project.getKey(), value(Project.TOTAL_AMOUNT, 0.00));
      repository.update(seriesKey, Series.FIRST_MONTH, null);
      repository.update(seriesKey, Series.LAST_MONTH, null);
      return;
    }

    ClosedMonthRange range = new ClosedMonthRange(months.first(), months.last());
    for (Integer monthId : range.asList()) {
      GlobList seriesBudgetList = SeriesBudget.getAll(seriesId, monthId, repository);
      if (seriesBudgetList.isEmpty()) {
        seriesBudgetList.add(repository.create(SeriesBudget.TYPE,
                                               value(SeriesBudget.SERIES, seriesId),
                                               value(SeriesBudget.MONTH, monthId)));
      }
    }

    double totalAmount = 0.00;
    for (Glob item : projectItems) {
      Double itemAmount = item.get(ProjectItem.AMOUNT, 0.0);
      totalAmount += itemAmount;

      Integer monthId = item.get(ProjectItem.MONTH);

      Glob seriesBudget = SeriesBudget.find(seriesId, monthId, repository);
      repository.update(seriesBudget.getKey(),
                        value(SeriesBudget.PLANNED_AMOUNT, seriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0) + itemAmount),
                        value(SeriesBudget.ACTIVE, true));
    }
    repository.update(project.getKey(), value(Project.TOTAL_AMOUNT, totalAmount));

    repository.update(seriesKey, Series.FIRST_MONTH, range.getMin());
    repository.update(seriesKey, Series.LAST_MONTH, range.getMax());
  }
}
