package org.designup.picsou.gui.card;

import org.designup.picsou.gui.categorization.actions.EditSeriesAction;
import org.designup.picsou.gui.categorization.actions.ShowMonthTransactionsInCategorizationViewAction;
import org.designup.picsou.gui.categorization.actions.ShowTransactionsInCategorizationViewAction;
import org.designup.picsou.gui.categorization.actions.ShowTransactionsToCategorizeAction;
import org.designup.picsou.gui.transactions.actions.ShowAccountTransactionsInAccountViewAction;
import org.designup.picsou.gui.transactions.actions.ShowAllTransactionsInAccountViewAction;
import org.designup.picsou.gui.transactions.actions.ShowSeriesTransactionsInAccountViewAction;
import org.designup.picsou.gui.transactions.actions.ShowTransactionsInAccountViewAction;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
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
    else if (type.equals(Transaction.TYPE)) {
      initTransactionsPopup(popup, monthIds, objectKeys);
    }
  }

  private void initBalancePopup(JPopupMenu popup, SortedSet<Integer> monthIds) {
    popup.add(new ShowMonthTransactionsInCategorizationViewAction(monthIds, repository, localDirectory));
    popup.add(new ShowAllTransactionsInAccountViewAction(localDirectory));
  }

  private void initMainAccountsPopup(JPopupMenu popup, SortedSet<Integer> monthIds) {
    addShowInCategorization(popup, monthIds, Matchers.transactionsForMainAccounts(repository));
    popup.add(new ShowAccountTransactionsInAccountViewAction(Matchers.userCreatedMainAccounts(),
                                                             repository, localDirectory));
  }

  private void initSavinsAccountsPopup(JPopupMenu popup, SortedSet<Integer> monthIds) {
    addShowInCategorization(popup, monthIds, Matchers.transactionsForSavingsAccounts(repository));
    popup.add(new ShowAccountTransactionsInAccountViewAction(Matchers.userCreatedSavingsAccounts(),
                                                             repository, localDirectory));
  }

  private void initUncategorizedPopup(JPopupMenu popup, SortedSet<Integer> monthIds) {
    popup.add(new ShowTransactionsToCategorizeAction(monthIds, repository, localDirectory));
    addShowInAccounts(popup, monthIds, Matchers.uncategorizedTransactions());
  }

  private void initBudgetAreasPopup(JPopupMenu popup, SortedSet<Integer> monthIds, Set<Key> objectKeys) {
    addShowTransactionActions(popup, monthIds,
                              Matchers.transactionsForBudgetAreas(GlobUtils.getValues(objectKeys, BudgetArea.ID)));
  }

  private void initSeriesPopup(JPopupMenu popup, SortedSet<Integer> monthIds, Set<Key> objectKeys) {
    Set<Integer> seriesIds = GlobUtils.getValues(objectKeys, Series.ID);
    GlobMatcher matcher = Matchers.transactionsForSeries(seriesIds);
    addShowInCategorization(popup, monthIds, matcher);
    popup.add(new ShowSeriesTransactionsInAccountViewAction(seriesIds, localDirectory));

    if (objectKeys.size() == 1) {
      popup.addSeparator();
      Key seriesKey = objectKeys.iterator().next();
      popup.add(new EditSeriesAction(seriesKey, monthIds, repository, localDirectory));
    }
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
