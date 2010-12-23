package org.designup.picsou.gui;

import org.designup.picsou.gui.components.filtering.FilterClearer;
import org.designup.picsou.gui.components.filtering.FilterManager;
import org.designup.picsou.gui.components.filtering.FilterSetBuilder;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.*;

import static org.designup.picsou.gui.utils.Matchers.*;

public class TransactionSelection implements GlobSelectionListener {

  private Set<Integer> currentMonths;
  private Set<Integer> currentBudgetAreas;
  private Set<Integer> currentSeries;

  private GlobRepository repository;
  private FilterManager filterManager;
  private SelectionService selectionService;

  public static final String MONTHS_FILTER = "months";
  public static final String SERIES_FILTER = "series";

  public TransactionSelection(FilterManager filterManager, GlobRepository repository, Directory directory) {
    this.filterManager = filterManager;
    this.repository = repository;
    init();
    selectionService = directory.get(SelectionService.class);
    selectionService.addListener(this, Account.TYPE, Month.TYPE,
                                 BudgetArea.TYPE, Series.TYPE);

    filterManager.addClearer(new SeriesFilterClearer());
  }

  public void init() {
    currentSeries = Collections.emptySet();
    currentBudgetAreas = Collections.singleton(BudgetArea.ALL.getId());
    currentMonths = Collections.emptySet();
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Month.TYPE)) {
      currentMonths = selection.getAll(Month.TYPE).getValueSet(Month.ID);
    }
    if (selection.isRelevantForType(BudgetArea.TYPE) || selection.isRelevantForType(Series.TYPE)) {
      currentBudgetAreas = selection.getAll(BudgetArea.TYPE).getValueSet(BudgetArea.ID);
      currentSeries = selection.getAll(Series.TYPE).getValueSet(Series.ID);
    }

    filterManager.set(FilterSetBuilder.init()
      .set(MONTHS_FILTER, transactionsForMonths(currentMonths))
      .set(SERIES_FILTER, transactionsForSeries(currentBudgetAreas, currentSeries, repository))
      .get());
  }

  private class SeriesFilterClearer implements FilterClearer {
    public List<String> getAssociatedFilters() {
      return Arrays.asList(SERIES_FILTER);
    }

    public void clear() {
      selectionService.select(GlobSelectionBuilder.init()
        .add(repository.get(BudgetArea.ALL.getKey()))
        .add(GlobList.EMPTY, Series.TYPE)
        .get());
    }
  }
}
