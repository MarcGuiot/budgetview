package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.utils.GlobMatcher;
import org.crossbowlabs.globs.model.utils.GlobMatchers;
import static org.crossbowlabs.globs.model.utils.GlobMatchers.and;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.designup.picsou.gui.model.MonthStat;
import static org.designup.picsou.gui.utils.PicsouMatchers.*;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Month;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TransactionSelection implements GlobSelectionListener {

  private Set<Integer> currentAccounts = Collections.singleton(Account.SUMMARY_ACCOUNT_ID);
  private Set<Integer> currentMonths = Collections.emptySet();
  private Set<Integer> currentCategories = Collections.singleton(Category.ALL);
  private List<GlobSelectionListener> listeners = new ArrayList<GlobSelectionListener>();
  private GlobRepository repository;
  private GlobMatcher currentMatcher = GlobMatchers.NONE;

  public TransactionSelection(GlobRepository repository, Directory directory) {
    this.repository = repository;
    directory.get(SelectionService.class).addListener(this, Account.TYPE, Month.TYPE, Category.TYPE);
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

    currentMatcher = and(transactionsForMonths(currentMonths),
                         transactionsForCategories(currentCategories, repository),
                         transactionsForAccounts(currentAccounts));

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
          result.add(repository.get(key));
        }
      }
    }
    return result;
  }

  public boolean isCategorySelected(Integer categoryId) {
    return currentCategories.contains(categoryId);
  }

  public Set<Integer> getCurrentAccounts() {
    return Collections.unmodifiableSet(currentAccounts);
  }

  public Set<Integer> getCurrentMonths() {
    return Collections.unmodifiableSet(currentMonths);
  }

  public Set<Integer> getCurrentCategories() {
    return Collections.unmodifiableSet(currentCategories);
  }
}
