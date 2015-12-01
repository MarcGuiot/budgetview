package org.designup.picsou.gui.projects.utils;

import org.designup.picsou.gui.model.ProjectItemStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.model.SeriesType;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.model.utils.GlobMatchers;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;
import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class ProjectUtils {
  public static void printProjectGlobs(int projectId, GlobRepository repository) {
    GlobList list = new GlobList();
    list.add(repository.get(Key.create(Project.TYPE, projectId)));
    GlobList items = repository.getAll(ProjectItem.TYPE, fieldEquals(ProjectItem.PROJECT, projectId));
    list.addAll(items);
    Set<Integer> seriesIds = new HashSet<Integer>();
    for (Glob item : items) {
      Glob series = repository.findLinkTarget(item, ProjectItem.SERIES);
      seriesIds.add(series.get(Series.ID));
      list.add(series);
      if (series.get(Series.MIRROR_SERIES) != null) {
        Glob mirror = repository.findLinkTarget(series, Series.MIRROR_SERIES);
        list.add(mirror);
        seriesIds.add(mirror.get(Series.ID));
      }
    }
    list.addAll(repository.getAll(Transaction.TYPE,
                                  GlobMatchers.and(fieldIn(Transaction.SERIES, seriesIds),
                                                   isFalse(Transaction.PLANNED))));
    list.addAll(repository.getAll(ProjectItemStat.TYPE,
                                  GlobMatchers.and(fieldIn(ProjectItemStat.PROJECT_ITEM, items.getValues(ProjectItem.ID)))));

    list.addAll(repository.getAll(SeriesStat.TYPE,
                                  GlobMatchers.and(fieldEquals(SeriesStat.TARGET_TYPE, SeriesType.SERIES.getId()),
                                                   fieldIn(SeriesStat.TARGET, seriesIds)))
                  .sortSelf(new GlobFieldsComparator(SeriesStat.TARGET_TYPE, true,
                                                 SeriesStat.TARGET, true,
                                                 SeriesStat.MONTH, true)));

    GlobPrinter.print(list);
  }
}
