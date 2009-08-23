package org.designup.picsou.gui;

import org.designup.picsou.gui.utils.PicsouMatchers;
import static org.designup.picsou.gui.utils.PicsouMatchers.*;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.and;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TransactionSelection implements GlobSelectionListener {

  private Set<Integer> currentAccounts;
  private Set<Integer> currentMonths;
  private Set<Integer> currentBudgetAreas;
  private Set<Integer> currentSeries;
  private List<GlobSelectionListener> listeners = new ArrayList<GlobSelectionListener>();
  private GlobRepository repository;
  private GlobMatcher currentMatcher;

  public TransactionSelection(GlobRepository repository, Directory directory) {
    init();
    this.repository = repository;
    directory.get(SelectionService.class).addListener(this, Account.TYPE, Month.TYPE,
                                                      BudgetArea.TYPE, Series.TYPE);
  }

  public void init(){
    currentMatcher = GlobMatchers.NONE;
    currentSeries = Collections.emptySet();
    currentBudgetAreas = Collections.singleton(BudgetArea.ALL.getId());
    currentMonths = Collections.emptySet();
    currentAccounts = Collections.singleton(Account.ALL_SUMMARY_ACCOUNT_ID);
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
    if (selection.isRelevantForType(BudgetArea.TYPE) || selection.isRelevantForType(Series.TYPE)) {
      currentBudgetAreas = selection.getAll(BudgetArea.TYPE).getValueSet(BudgetArea.ID);
      currentSeries = selection.getAll(Series.TYPE).getValueSet(Series.ID);
    }

    currentMatcher = and(transactionsForMonths(currentMonths),
                         transactionsForAccounts(currentAccounts, repository),
                         PicsouMatchers.transactionsForSeries(currentBudgetAreas, currentSeries, repository));

    for (GlobSelectionListener listener : listeners) {
      listener.selectionUpdated(selection);
    }
  }

  public GlobMatcher getCurrentMatcher() {
    return currentMatcher;
  }
}
