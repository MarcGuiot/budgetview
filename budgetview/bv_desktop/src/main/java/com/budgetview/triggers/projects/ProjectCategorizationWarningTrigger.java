package com.budgetview.triggers.projects;

import com.budgetview.gui.model.ProjectItemStat;
import com.budgetview.shared.utils.Amounts;
import com.budgetview.gui.model.SeriesStat;
import com.budgetview.model.ProjectItem;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class ProjectCategorizationWarningTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (!changeSet.containsChanges(ProjectItem.TYPE) &&
        !changeSet.containsChanges(SeriesStat.TYPE)) {
      return;
    }

    final Set<Integer> modifiedProjectItemIds = new HashSet<Integer>();
    Set<Integer> seriesIds = getChangedSeriesIds(changeSet);
    for (Glob item : repository.getAll(ProjectItem.TYPE, fieldIn(ProjectItem.SERIES, seriesIds))) {
      modifiedProjectItemIds.add(item.get(ProjectItem.ID));
    }
    for (Key key : changeSet.getUpdated(ProjectItem.FIRST_MONTH)) {
      modifiedProjectItemIds.add(key.get(ProjectItem.ID));
    }
    for (Key key : changeSet.getUpdated(ProjectItem.MONTH_COUNT)) {
      modifiedProjectItemIds.add(key.get(ProjectItem.ID));
    }
    for (Integer projectItemId : modifiedProjectItemIds) {
      checkCategorizationWarning(repository.get(Key.create(ProjectItem.TYPE, projectItemId)), repository);
    }
  }

  protected Set<Integer> getChangedSeriesIds(ChangeSet changeSet) {
    Set<Key> changedKeys = changeSet.getChanged(SeriesStat.TYPE);
    Set<Integer> result = new HashSet<Integer>();
    for (Key key : changedKeys) {
      if (SeriesStat.isSummaryForSeries(key)) {
        result.add(key.get(SeriesStat.TARGET));
      }
    }
    return result;
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(ProjectItem.TYPE)) {
      for (Glob item : repository.getAll(ProjectItem.TYPE)) {
        checkCategorizationWarning(item, repository);
      }
    }
  }

  private void checkCategorizationWarning(Glob projectItem, GlobRepository repository) {
    boolean warning = false;

    if (projectItem.get(ProjectItem.SERIES) != null) {
      Glob series = repository.findLinkTarget(projectItem, ProjectItem.SERIES);
      if (series != null) {
        for (Glob seriesStat : SeriesStat.getAllSummaryMonthsForSeries(series, repository)) {
          if (!monthInProjectItemRange(seriesStat.get(SeriesStat.MONTH), projectItem)
              && Amounts.isNotZero(seriesStat.get(SeriesStat.ACTUAL_AMOUNT))) {
            warning = true;
          }
        }
      }
    }

    repository.update(Key.create(ProjectItemStat.TYPE, projectItem.get(ProjectItem.ID)),
                      ProjectItemStat.CATEGORIZATION_WARNING,
                      warning);
  }

  private boolean monthInProjectItemRange(Integer monthId, Glob projectItem) {
    Integer firstMonth = projectItem.get(ProjectItem.FIRST_MONTH);
    if (firstMonth == null) {
      return false;
    }
    if (monthId < firstMonth) {
      return false;
    }
    Integer lastMonth = ProjectItem.getLastMonth(projectItem);
    return monthId <= lastMonth;
  }
}
