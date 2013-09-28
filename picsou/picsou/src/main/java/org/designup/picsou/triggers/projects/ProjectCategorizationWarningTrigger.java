package org.designup.picsou.triggers.projects;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.model.ProjectItemStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.model.SubSeriesStat;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SubSeries;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobUtils;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class ProjectCategorizationWarningTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (!changeSet.containsChanges(ProjectItem.TYPE) &&
        !changeSet.containsChanges(SubSeriesStat.TYPE) &&
        !changeSet.containsChanges(SeriesStat.TYPE)) {
      return;
    }

    final Set<Integer> modifiedProjectItemIds = new HashSet<Integer>();
    Set<Integer> subSeriesIds = GlobUtils.getValues(changeSet.getChanged(SubSeriesStat.TYPE), SubSeriesStat.SUB_SERIES);
    for (Glob item : repository.getAll(ProjectItem.TYPE, fieldIn(ProjectItem.SUB_SERIES, subSeriesIds))) {
      modifiedProjectItemIds.add(item.get(ProjectItem.ID));
    }
    Set<Integer> seriesIds = GlobUtils.getValues(changeSet.getChanged(SeriesStat.TYPE), SeriesStat.SERIES);
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

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(ProjectItem.TYPE)) {
      for (Glob item : repository.getAll(ProjectItem.TYPE)) {
        checkCategorizationWarning(item, repository);
      }
    }
  }

  private void checkCategorizationWarning(Glob projectItem, GlobRepository repository) {
    boolean warning = false;

    if (ProjectItem.usesSubSeries(projectItem)) {
      if (projectItem.get(ProjectItem.SUB_SERIES) != null) {
        Glob subSeries = repository.get(Key.create(SubSeries.TYPE, projectItem.get(ProjectItem.SUB_SERIES)));
        for (Glob subSeriesStat : repository.findLinkedTo(subSeries, SubSeriesStat.SUB_SERIES)) {
          if (!monthInProjectItemRange(subSeriesStat.get(SubSeriesStat.MONTH), projectItem)
              && Amounts.isNotZero(subSeriesStat.get(SubSeriesStat.ACTUAL_AMOUNT))) {
            warning = true;
          }
        }
      }
    }
    else {
      Glob series = repository.find(Key.create(Series.TYPE, projectItem.get(ProjectItem.SERIES)));
      if (series != null) {
        for (Glob seriesStat : repository.findLinkedTo(series, SeriesStat.SERIES)) {
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
