package org.designup.picsou.gui.series;

import org.designup.picsou.gui.projects.ProjectEditionDialog;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Project;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class SeriesEditor {

  private SeriesEditionDialog seriesEditionDialog;
  private SeriesAmountEditionDialog seriesAmountEditionDialog;
  private ProjectEditionDialog projectEditionDialog;
  private GlobRepository repository;
  private Integer lastSelectedSubSeriesId;

  public SeriesEditor(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.seriesEditionDialog = new SeriesEditionDialog(repository, directory);
    this.seriesAmountEditionDialog = new SeriesAmountEditionDialog(repository, directory, seriesEditionDialog);
    this.projectEditionDialog = new ProjectEditionDialog(repository, directory);
  }

  public Key showNewSeries(GlobList transactions, GlobList months, BudgetArea budgetArea, FieldValue... forcedValues) {
    Key createdKey = seriesEditionDialog.showNewSeries(transactions, months, budgetArea, forcedValues);
    lastSelectedSubSeriesId = seriesEditionDialog.getLastSelectedSubSeriesId();
    return createdKey;
  }

  public void show(Glob series, Set<Integer> selectedMonthIds) {
    Glob project = Project.findProject(series, repository);
    if (project != null) {
      projectEditionDialog.show(project.getKey());
      lastSelectedSubSeriesId = null;
    }
    else {
      seriesEditionDialog.show(series, selectedMonthIds);
      lastSelectedSubSeriesId = seriesEditionDialog.getLastSelectedSubSeriesId();
    }
  }

  public void showAmount(Glob series, Set<Integer> selectedMonthIds) {
    Glob project = Project.findProject(series, repository);
    if (project != null) {
      projectEditionDialog.show(project.getKey());
    }
    else {
      seriesAmountEditionDialog.show(series, selectedMonthIds);
    }
    lastSelectedSubSeriesId = null;
  }

  public Integer getLastSelectedSubSeriesId() {
    return lastSelectedSubSeriesId;
  }
}
