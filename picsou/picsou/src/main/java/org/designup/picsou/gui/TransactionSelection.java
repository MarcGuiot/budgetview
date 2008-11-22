package org.designup.picsou.gui;

import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.gui.utils.PicsouMatchers;
import static org.designup.picsou.gui.utils.PicsouMatchers.*;
import org.designup.picsou.model.*;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.and;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TransactionSelection implements GlobSelectionListener {

  private Set<Integer> currentAccounts = Collections.singleton(Account.ALL_SUMMARY_ACCOUNT_ID);
  private Set<Integer> currentMonths = Collections.emptySet();
  private Set<Integer> currentCategories = Collections.singleton(Category.ALL);
  private Set<Integer> currentBudgetAreas = Collections.singleton(BudgetArea.ALL.getId());
  private Set<Integer> currentSeries = Collections.emptySet();
  private List<GlobSelectionListener> listeners = new ArrayList<GlobSelectionListener>();
  private GlobRepository repository;
  private GlobMatcher currentMatcher = GlobMatchers.NONE;

  public TransactionSelection(GlobRepository repository, Directory directory) {
    this.repository = repository;
    directory.get(SelectionService.class).addListener(this, Account.TYPE, Month.TYPE, Category.TYPE,
                                                      BudgetArea.TYPE, Series.TYPE);
  }

  public void addListener(GlobSelectionListener listener) {
    listeners.add(listener);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Account.TYPE)) {
      currentAccounts = selection.getAll(Account.TYPE).getValueSet(Account.ID);
    }
    if (selection.isRelevantForType(Month.TYPE)) {
      currentMonths = selection.getAll(Month.TYPE).getValueSet(Month.ID);
    }
    if (selection.isRelevantForType(Category.TYPE)) {
      currentCategories = selection.getAll(Category.TYPE).getValueSet(Category.ID);
    }
    if (selection.isRelevantForType(BudgetArea.TYPE) || selection.isRelevantForType(Series.TYPE)) {
      currentBudgetAreas = selection.getAll(BudgetArea.TYPE).getValueSet(BudgetArea.ID);
      currentSeries = selection.getAll(Series.TYPE).getValueSet(Series.ID);
    }

    currentMatcher = and(transactionsForMonths(currentMonths),
                         transactionsForCategories(currentCategories, repository),
                         transactionsForAccounts(currentAccounts, repository),
                         PicsouMatchers.transactionsForSeries(currentBudgetAreas, currentSeries, repository));

    for (GlobSelectionListener listener : listeners) {
      listener.selectionUpdated(selection);
    }
  }

  public GlobMatcher getCurrentMatcher() {
    return currentMatcher;
  }

  public GlobList getSelectedMonthStats() {
    GlobList result = new GlobList();
    for (Integer accountId : currentAccounts) {
      for (Integer categoryId : currentCategories) {
        for (Integer month : currentMonths) {
          Key key = MonthStat.getKey(month, categoryId, accountId);
          result.add(repository.get(key));
        }
      }
    }
    return result;
  }

  public GlobList getMonthStatsForAllMasterCategories() {
    GlobList result = new GlobList();
    for (Integer accountId : currentAccounts) {
      for (MasterCategory master : MasterCategory.values()) {
        for (Integer month : currentMonths) {
          Key key = MonthStat.getKey(month, master.getId(), accountId);
          Glob monthStat = repository.find(key);
          if (monthStat != null) {
            result.add(monthStat);
          }
        }
      }
    }
    return result;
  }

  public boolean isCategorySelected(Integer categoryId) {
    return currentCategories.contains(categoryId);
  }
}
