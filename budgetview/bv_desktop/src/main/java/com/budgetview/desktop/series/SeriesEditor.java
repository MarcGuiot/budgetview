package com.budgetview.desktop.series;

import com.budgetview.desktop.card.NavigationService;
import com.budgetview.model.Account;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.model.ProjectItem;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class SeriesEditor {

  private SeriesEditionDialog seriesEditionDialog;
  private GlobRepository repository;
  private Integer lastSelectedSubSeriesId;
  private NavigationService navigationService;

  public SeriesEditor(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.seriesEditionDialog = new SeriesEditionDialog(repository, directory);
    this.navigationService = directory.get(NavigationService.class);
  }

  public Key showNewSeries(GlobList transactions, GlobList months, BudgetArea budgetArea, FieldValue... forcedValues) {
    Key createdKey = seriesEditionDialog.showNewSeries(transactions, months, budgetArea, forcedValues);
    lastSelectedSubSeriesId = seriesEditionDialog.getLastSelectedSubSeriesId();
    return createdKey;
  }

  public void showSeries(Glob series, Set<Integer> selectedMonthIds) {
    Glob projectItem = ProjectItem.findProjectItem(series, repository);
    if (projectItem != null) {
      navigationService.gotoProjectItem(projectItem);
      lastSelectedSubSeriesId = null;
      return;
    }

    seriesEditionDialog.show(series, selectedMonthIds);
    lastSelectedSubSeriesId = seriesEditionDialog.getLastSelectedSubSeriesId();
  }

  public void showAmount(Glob series, Set<Integer> selectedMonthIds) {
    Glob projectItem = ProjectItem.findProjectItem(series, repository);
    if (projectItem != null) {
      navigationService.gotoProjectItem(projectItem);
      lastSelectedSubSeriesId = null;
      return;
    }

    if (Account.needsTargetAccount(series)) {
      seriesEditionDialog.show(series, selectedMonthIds);
      lastSelectedSubSeriesId = seriesEditionDialog.getLastSelectedSubSeriesId();
      return;
    }

    seriesEditionDialog.show(series, selectedMonthIds);
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
