package org.designup.picsou.gui.series.utils;

import org.designup.picsou.model.Project;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesGroup;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

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
}
