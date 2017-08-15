package com.budgetview.desktop.series.utils;

import com.budgetview.model.Project;
import com.budgetview.model.ProjectItem;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesGroup;
import com.budgetview.shared.model.BudgetArea;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class SeriesErrorsUpgrade {
  public static void fixMissingGroups(GlobRepository repository) {
    for (Glob series : repository.getAll(Series.TYPE)) {
      Integer groupId = series.get(Series.GROUP);
      if ((groupId != null) && !repository.contains(Key.create(SeriesGroup.TYPE, groupId))) {
        fixMissingGroup(series, repository);
      }
    }
  }

  private static void fixMissingGroup(Glob series, GlobRepository repository) {
    GlobList items = repository.findLinkedTo(series, ProjectItem.SERIES);
    if (items.isEmpty()) {
      repository.update(series.getKey(), Series.GROUP, null);
      return;
    }

    for (Glob item : items) {
      if (ProjectItem.usesExtrasSeries(item)) {
        Glob project = repository.findLinkTarget(item, ProjectItem.PROJECT);
        repository.update(series.getKey(), Series.GROUP, project.get(Project.SERIES_GROUP));
      }
      else {
        repository.update(series.getKey(), Series.GROUP, null);
      }
    }
  }

  public static void fixInvalidTransfers(GlobRepository repository) {
    for (Glob series : repository.getAll(Series.TYPE, linkedTo(BudgetArea.TRANSFER.getKey(), Series.BUDGET_AREA))) {
      if (series.exists() && !Series.isValid(series)) {
        Series.delete(series, repository);
      }
    }
  }
}
