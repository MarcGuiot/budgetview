package com.budgetview.desktop.card;

import com.budgetview.desktop.accounts.utils.AccountMatchers;
import com.budgetview.desktop.categorization.actions.EditSeriesAction;
import com.budgetview.desktop.categorization.actions.ShowMonthTransactionsInCategorizationViewAction;
import com.budgetview.desktop.categorization.actions.ShowTransactionsInCategorizationViewAction;
import com.budgetview.desktop.categorization.actions.ShowTransactionsToCategorizeAction;
import com.budgetview.desktop.transactions.actions.ShowAccountTransactionsInAccountViewAction;
import com.budgetview.desktop.transactions.actions.ShowAllTransactionsInAccountViewAction;
import com.budgetview.desktop.transactions.actions.ShowSeriesTransactionsInAccountViewAction;
import com.budgetview.desktop.transactions.actions.ShowTransactionsInAccountViewAction;
import com.budgetview.desktop.transactions.utils.TransactionMatchers;
import com.budgetview.model.Account;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesGroup;
import com.budgetview.model.Transaction;
import com.budgetview.shared.model.BudgetArea;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

public class NavigationPopup {
  private JComponent clickedComponent;
  private GlobRepository repository;
  private Directory localDirectory;

  public NavigationPopup(JComponent clickedComponent, GlobRepository repository, Directory directory, SelectionService parentSelectionService) {
    this.clickedComponent = clickedComponent;
    this.repository = repository;
    this.localDirectory = new DefaultDirectory(directory);
    this.localDirectory.add(SelectionService.class, parentSelectionService);
  }

  public void show(SortedSet<Integer> columnIds, Set<Key> objectKeys) {
    if (objectKeys.isEmpty()) {
      return;
    }

    JPopupMenu popup = new JPopupMenu();
    initPopup(popup, columnIds, objectKeys);
    if (popup.getSubElements().length != 0) {
      Point position = clickedComponent.getMousePosition();
      int x = position != null ? position.x : 0;
      int y = position != null ? position.y : 0;
      popup.show(clickedComponent, x, y);
    }
  }

  public void initPopup(JPopupMenu popup, SortedSet<Integer> monthIds, Set<Key> objectKeys) {
    if (objectKeys.size() == 1) {
      Key key = objectKeys.iterator().next();
      if (BudgetArea.ALL.getKey().equals(key)) {
        initBalancePopup(popup, monthIds);
        return;
      }
      if (Account.MAIN_SUMMARY_KEY.equals(key)) {
        initMainAccountsPopup(popup, monthIds);
        return;
      }
      if (Account.SAVINGS_SUMMARY_KEY.equals(key)) {
        initSavinsAccountsPopup(popup, monthIds);
        return;
      }
      if (BudgetArea.UNCATEGORIZED.getKey().equals(key) || Series.UNCATEGORIZED_SERIES.equals(key)) {
        initUncategorizedPopup(popup, monthIds);
        return;
      }
    }

    Set<GlobType> types = GlobUtils.getTypes(objectKeys);
    if (types.size() != 1) {
      return;
    }

    GlobType type = types.iterator().next();
    if (type.equals(BudgetArea.TYPE)) {
      initBudgetAreasPopup(popup, monthIds, objectKeys);
    }
    else if (type.equals(Series.TYPE)) {
      initSeriesPopup(popup, monthIds, objectKeys);
    }
    if (type.equals(SeriesGroup.TYPE)) {
      initSeriesGroupPopup(popup, monthIds, objectKeys);
    }
    else if (type.equals(Transaction.TYPE)) {
      initTransactionsPopup(popup, monthIds, objectKeys);
    }
  }

  private void initBalancePopup(JPopupMenu popup, SortedSet<Integer> monthIds) {
    popup.add(new ShowMonthTransactionsInCategorizationViewAction(monthIds, repository, localDirectory));
    popup.add(new ShowAllTransactionsInAccountViewAction(localDirectory));
  }

  private void initMainAccountsPopup(JPopupMenu popup, SortedSet<Integer> monthIds) {
    addShowInCategorization(popup, monthIds, TransactionMatchers.transactionsForMainAccounts(repository));
    popup.add(new ShowAccountTransactionsInAccountViewAction(AccountMatchers.userCreatedMainAccounts(),
                                                             repository, localDirectory));
  }

  private void initSavinsAccountsPopup(JPopupMenu popup, SortedSet<Integer> monthIds) {
    addShowInCategorization(popup, monthIds, TransactionMatchers.transactionsForSavingsAccounts(repository));
    popup.add(new ShowAccountTransactionsInAccountViewAction(AccountMatchers.userCreatedSavingsAccounts(),
                                                             repository, localDirectory));
  }

  private void initUncategorizedPopup(JPopupMenu popup, SortedSet<Integer> monthIds) {
    popup.add(new ShowTransactionsToCategorizeAction(monthIds, repository, localDirectory));
    addShowInAccounts(popup, monthIds, TransactionMatchers.uncategorized());
  }

  private void initBudgetAreasPopup(JPopupMenu popup, SortedSet<Integer> monthIds, Set<Key> objectKeys) {
    addShowTransactionActions(popup, monthIds,
                              TransactionMatchers.transactionsForBudgetAreas(GlobUtils.getValues(objectKeys, BudgetArea.ID)));
  }

  private void initSeriesPopup(JPopupMenu popup, SortedSet<Integer> monthIds, Set<Key> objectKeys) {
    Set<Integer> seriesIds = GlobUtils.getValues(objectKeys, Series.ID);
    GlobMatcher matcher = TransactionMatchers.transactionsForSeries(seriesIds);
    addShowInCategorization(popup, monthIds, matcher);
    popup.add(new ShowSeriesTransactionsInAccountViewAction(seriesIds, localDirectory));

    if (objectKeys.size() == 1) {
      popup.addSeparator();
      Key seriesKey = objectKeys.iterator().next();
      popup.add(new EditSeriesAction(seriesKey, monthIds, repository, localDirectory));
    }
  }

  private void initSeriesGroupPopup(JPopupMenu popup, SortedSet<Integer> monthIds, Set<Key> objectKeys) {
    Set<Integer> seriesIds = new HashSet<Integer>();
    for (Key groupKey : objectKeys) {
      Glob group = repository.get(groupKey);
      seriesIds.addAll(repository.findLinkedTo(group, Series.GROUP).getValueSet(Series.ID));
    }
    GlobUtils.getValues(objectKeys, SeriesGroup.ID);
    GlobMatcher matcher = TransactionMatchers.transactionsForSeries(seriesIds);
    addShowInCategorization(popup, monthIds, matcher);
    popup.add(new ShowSeriesTransactionsInAccountViewAction(seriesIds, localDirectory));
  }

  private void initTransactionsPopup(JPopupMenu popup, SortedSet<Integer> monthIds, Set<Key> objectKeys) {
    addShowTransactionActions(popup, monthIds, GlobMatchers.keyIn(objectKeys));
  }

  private void addShowTransactionActions(JPopupMenu popup, SortedSet<Integer> monthIds, GlobMatcher matcher) {
    addShowInCategorization(popup, monthIds, matcher);
    addShowInAccounts(popup, monthIds, matcher);
  }

  private void addShowInAccounts(JPopupMenu popup, SortedSet<Integer> monthIds, GlobMatcher matcher) {
    popup.add(new ShowTransactionsInAccountViewAction(monthIds, matcher, repository, localDirectory));
  }

  private void addShowInCategorization(JPopupMenu popup, SortedSet<Integer> monthIds, GlobMatcher matcher) {
    popup.add(new ShowTransactionsInCategorizationViewAction(monthIds, matcher, repository, localDirectory));
  }
}
