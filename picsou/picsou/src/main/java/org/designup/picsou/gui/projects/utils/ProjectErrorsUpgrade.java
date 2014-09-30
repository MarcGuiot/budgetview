package org.designup.picsou.gui.projects.utils;

import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.ProjectTransfer;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.triggers.projects.ProjectItemToSeriesTrigger;
import org.designup.picsou.triggers.projects.ProjectToSeriesGroupTrigger;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Utils;

public class ProjectErrorsUpgrade {

  public static void createMissingGroupsAndSeries(GlobRepository repository) {
    ProjectToSeriesGroupTrigger.createGroupsForProjects(repository);
    ProjectItemToSeriesTrigger.createMissingSeries(repository);
  }

  public static void fixIncoherentFromToInTransferSeries(GlobRepository repository) {
    for (Glob transfer : repository.getAll(ProjectTransfer.TYPE)) {
      Glob item = ProjectTransfer.getItemFromTransfer(transfer, repository);
      Glob series = repository.findLinkTarget(item, ProjectItem.SERIES);
      Integer from = series.get(Series.FROM_ACCOUNT);
      Integer to = series.get(Series.TO_ACCOUNT);
      Integer target = series.get(Series.TARGET_ACCOUNT);
      for (Glob seriesBudget : repository.findLinkedTo(series, SeriesBudget.SERIES)) {
         Double amount = seriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0.00);
        if (((amount < 0) && Utils.equal(target, to)) ||
            ((amount > 0) && Utils.equal(target, from))) {
          repository.update(seriesBudget.getKey(), SeriesBudget.PLANNED_AMOUNT, -amount);
        }
      }
    }
  }
}
