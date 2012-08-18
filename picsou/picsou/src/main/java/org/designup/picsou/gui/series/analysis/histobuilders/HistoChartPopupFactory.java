package org.designup.picsou.gui.series.analysis.histobuilders;

import org.designup.picsou.gui.categorization.actions.EditSeriesAction;
import org.designup.picsou.gui.categorization.actions.ShowTransactionsInCategorizationViewAction;
import org.designup.picsou.gui.categorization.actions.ShowTransactionsToCategorizeAction;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.gui.transactions.actions.ShowAccountTransactionsInAccountViewAction;
import org.designup.picsou.gui.transactions.actions.ShowTransactionsInAccountViewAction;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
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

public class HistoChartPopupFactory {
  private HistoChart histoChart;
  private GlobRepository repository;
  private Directory localDirectory;

  public HistoChartPopupFactory(HistoChart histoChart, GlobRepository repository, Directory directory, SelectionService parentSelectionService) {
    this.histoChart = histoChart;
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
      Point position = histoChart.getMousePosition();
      int x = position != null ? position.x : 0;
      int y = position != null ? position.y : 0;
      popup.show(histoChart, x, y);
    }
  }

  private void initPopup(JPopupMenu popup, SortedSet<Integer> monthIds, Set<Key> objectKeys) {
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
      if (Series.UNCATEGORIZED_SERIES.equals(key)) {
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
  }

  private void initBalancePopup(JPopupMenu popup, SortedSet<Integer> monthIds) {
    addShowTransactionActions(popup, monthIds, GlobMatchers.ALL);
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
                              Matchers.transactionsForBudgetAreas(GlobUtils.getIntegerValues(objectKeys, BudgetArea.ID)));
  }

  private void initSeriesPopup(JPopupMenu popup, SortedSet<Integer> monthIds, Set<Key> objectKeys) {
    addShowTransactionActions(popup, monthIds,
                              Matchers.transactionsForSeries(GlobUtils.getIntegerValues(objectKeys, Series.ID)));
    
    if (objectKeys.size() == 1) {
      popup.addSeparator();
      Key seriesKey = objectKeys.iterator().next();
      popup.add(new EditSeriesAction(seriesKey, monthIds, repository, localDirectory));
    }
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