package com.budgetview.desktop.utils.datacheck.check;

import com.budgetview.desktop.utils.datacheck.DataCheckReport;
import com.budgetview.model.Project;
import com.budgetview.model.ProjectItem;
import com.budgetview.triggers.projects.ProjectToSeriesGroupTrigger;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import static com.budgetview.model.ProjectItemType.isExpenses;

public class ProjectCheck {
  public static void allProjectsHaveSeriesGroup(GlobRepository repository, DataCheckReport report) {
    for (Glob project : repository.getAll(ProjectItem.TYPE, isExpenses()).getTargets(ProjectItem.PROJECT, repository)) {
      if (repository.findLinkTarget(project, Project.SERIES_GROUP) == null) {
        report.addFix("Missing SeriesGroup for project with expense items, creating it", Project.toString(project));
        ProjectToSeriesGroupTrigger.createGroup(project, repository);
      }
    }
  }
}
