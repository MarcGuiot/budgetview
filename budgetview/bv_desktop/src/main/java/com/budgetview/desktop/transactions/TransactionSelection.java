package com.budgetview.desktop.transactions;

import com.budgetview.desktop.components.filtering.FilterClearer;
import com.budgetview.desktop.components.filtering.FilterManager;
import com.budgetview.desktop.transactions.utils.TransactionMatchers;
import com.budgetview.model.*;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import java.util.*;

public class TransactionSelection implements GlobSelectionListener {

  private Set<Integer> currentMonths;

  private GlobRepository repository;
  private FilterManager filterManager;
  private SelectionService selectionService;
  private Set<Integer> selectedSeriesIds = new HashSet<Integer>();

  private static final String MONTHS_FILTER = "months";
  private static final String SERIES_FILTER = "series";

  public TransactionSelection(FilterManager filterManager, GlobRepository repository, Directory directory) {
    this.filterManager = filterManager;
    this.repository = repository;
    init();
    selectionService = directory.get(SelectionService.class);
    selectionService.addListener(this, Account.TYPE, Month.TYPE,
                                 BudgetArea.TYPE, Series.TYPE);

    filterManager.addClearer(new SeriesFilterClearer());

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsDeletions(Series.TYPE)) {
          Set<Key> deleted = changeSet.getDeleted(Series.TYPE);
          for (Key key : deleted) {
            if (selectedSeriesIds.contains(key.get(Series.ID))) {
              clearSelection();
              return;
            }
          }
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(Series.TYPE)) {
          clearSelection();
        }
      }
    });
  }

  public void init() {
    currentMonths = Collections.emptySet();
    selectedSeriesIds.clear();
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Month.TYPE)) {
      currentMonths = selection.getAll(Month.TYPE).getValueSet(Month.ID);
    }

    filterManager.set(MONTHS_FILTER, "", TransactionMatchers.transactionsForMonths(currentMonths));
  }

  public void setTransactionsFilter(GlobList transactions) {
    String label = transactions.size() == 1 ? Lang.get("filter.transaction.one") : Lang.get("filter.transaction.several", transactions.size());
    filterManager.set(TransactionSelection.SERIES_FILTER, label, GlobMatchers.fieldIn(Transaction.ID, transactions.getValueSet(Transaction.ID)));
    selectedSeriesIds.clear();
  }

  public void setSeriesFilter(Glob series) {
    String label = Lang.get("filter.series.single", series.get(Series.NAME));
    filterManager.set(TransactionSelection.SERIES_FILTER, label, TransactionMatchers.transactionsForSeries(series.get(Series.ID)));
    selectedSeriesIds.clear();
    selectedSeriesIds.add(series.get(Series.ID));
  }

  public void setSeriesFilter(Set<Integer> seriesIds) {
    String label = seriesIds.size() == 1 ? Lang.get("filter.series.one") : Lang.get("filter.series.several", seriesIds.size());
    filterManager.set(TransactionSelection.SERIES_FILTER, label, TransactionMatchers.transactionsForSeries(seriesIds));
    selectedSeriesIds.clear();
    selectedSeriesIds.addAll(seriesIds);
  }

  private class SeriesFilterClearer implements FilterClearer {
    public List<String> getAssociatedFilters() {
      return Arrays.asList(SERIES_FILTER);
    }

    public void clear() {
      clearSelection();
    }
  }

  private void clearSelection() {
    filterManager.remove(SERIES_FILTER);
    selectedSeriesIds.clear();
    selectionService.select(GlobSelectionBuilder.init()
                              .add(repository.get(BudgetArea.ALL.getKey()))
                              .add(GlobList.EMPTY, Series.TYPE)
                              .get());
  }
}
