package org.designup.picsou.gui.series.utils;

import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

public class SeriesSelectionConverter {
  private SelectionService parentSelectionService;
  private SelectionService localSelectionService;
  private GlobRepository repository;
  private boolean conversionInProgress;

  public SeriesSelectionConverter(SelectionService parentSelectionService,
                                  SelectionService localSelectionService,
                                  GlobRepository repository) {
    this.repository = repository;
    this.parentSelectionService = parentSelectionService;
    this.localSelectionService = localSelectionService;
  }

  public void register() {

    localSelectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        if (conversionInProgress) {
          return;
        }

        GlobSelectionBuilder newSelection = new GlobSelectionBuilder();
        for (Glob wrapper : selection.getAll(SeriesWrapper.TYPE)) {
          Integer itemId = wrapper.get(SeriesWrapper.ITEM_ID);
          if (SeriesWrapperType.BUDGET_AREA.isOfType(wrapper)) {
            Glob budgetArea = repository.get(Key.create(BudgetArea.TYPE, itemId));
            newSelection.add(budgetArea);
          }
          else if (SeriesWrapperType.SERIES.isOfType(wrapper)) {
            Glob series = repository.get(Key.create(Series.TYPE, itemId));
            newSelection.add(series);
          }
        }

        try {
          conversionInProgress = true;
          parentSelectionService.select(newSelection.get());
        }
        finally {
          conversionInProgress = false;
        }
      }
    }, SeriesWrapper.TYPE);

    parentSelectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        if (conversionInProgress) {
          return;
        }

        GlobList seriesList = selection.getAll(Series.TYPE);
        GlobSelectionBuilder newSelection = new GlobSelectionBuilder();
        newSelection.add(SeriesWrapper.getWrapperForBudgetArea(BudgetArea.ALL, repository));
        if (seriesList.isEmpty()) {
          newSelection.add(GlobList.EMPTY, Series.TYPE);
        }
        else {
          for (Glob series : seriesList) {
            newSelection.add(SeriesWrapper.getWrapperForSeries(series.get(Series.ID), repository));
          }
        }

        try {
          conversionInProgress = true;
          localSelectionService.select(newSelection.get());
        }
        finally {
          conversionInProgress = false;
        }

      }
    }, Series.TYPE);
  }
}
