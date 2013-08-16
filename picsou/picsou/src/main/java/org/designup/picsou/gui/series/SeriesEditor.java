package org.designup.picsou.gui.series;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Project;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class SeriesEditor {

  private SeriesEditionDialog seriesEditionDialog;
  private SeriesAmountEditionDialog seriesAmountEditionDialog;
  private GlobRepository repository;
  private Integer lastSelectedSubSeriesId;
  private NavigationService navigationService;

  public SeriesEditor(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.seriesEditionDialog = new SeriesEditionDialog(repository, directory);
    this.seriesAmountEditionDialog = new SeriesAmountEditionDialog(repository, directory, seriesEditionDialog);
    this.navigationService = directory.get(NavigationService.class);
  }

  public Key showNewSeries(GlobList transactions, GlobList months, BudgetArea budgetArea, FieldValue... forcedValues) {
    Key createdKey = seriesEditionDialog.showNewSeries(transactions, months, budgetArea, forcedValues);
    lastSelectedSubSeriesId = seriesEditionDialog.getLastSelectedSubSeriesId();
    return createdKey;
  }

  public void showSeries(Glob series, Set<Integer> selectedMonthIds) {
    Glob project = Project.findProject(series, repository);
    if (project != null) {
      navigationService.gotoProject(project.getKey());
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
      navigationService.gotoProject(project.getKey());
    }
    else {
      seriesAmountEditionDialog.show(series, selectedMonthIds);
    }
    lastSelectedSubSeriesId = null;
  }

  public void showNewProject() {
    navigationService.gotoNewProject();
  }

  public Integer getLastSelectedSubSeriesId() {
    return lastSelectedSubSeriesId;
  }

  public static SeriesEditor get(Directory directory) {
    return directory.get(SeriesEditor.class);
  }
}
