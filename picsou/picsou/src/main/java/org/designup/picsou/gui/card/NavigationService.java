package org.designup.picsou.gui.card;

import org.designup.picsou.gui.budget.BudgetToggle;
import org.designup.picsou.gui.categorization.CategorizationSelectionView;
import org.designup.picsou.gui.categorization.components.CategorizationFilteringMode;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.projects.ProjectView;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.ClosedMonthRange;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import java.util.*;

public class NavigationService implements GlobSelectionListener {

  public static final Card INITIAL_CARD = Card.HOME;

  private SelectionService selectionService;
  private TransactionView transactionView;
  private CategorizationSelectionView categorizationSelectionView;
  private ProjectView projectView;
  private BudgetToggle budgetToggle;
  private GlobRepository repository;

  private Card currentCard = INITIAL_CARD;
  private Stack<Card> backStack = new Stack<Card>();
  private Stack<Card> forwardStack = new Stack<Card>();

  public NavigationService(TransactionView transactionView,
                           CategorizationSelectionView categorizationSelectionView,
                           ProjectView projectView,
                           BudgetToggle budgetToggle,
                           GlobRepository repository,
                           Directory directory) {
    this.transactionView = transactionView;
    this.categorizationSelectionView = categorizationSelectionView;
    this.projectView = projectView;
    this.budgetToggle = budgetToggle;
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(this, Card.TYPE);
  }

  public void gotoCard(Card card) {
    select(card, false);
  }

  public void gotoHome() {
    gotoCard(Card.HOME);
  }

  public void gotoBudget() {
    gotoCard(Card.BUDGET);
  }

  public void gotoBudgetForMainAccounts() {
    gotoCard(Card.BUDGET);
    budgetToggle.showMain();
  }

  public void gotoBudgetForSavingsAccounts() {
    gotoCard(Card.BUDGET);
    budgetToggle.showSavings();
  }

  public void gotoCategorization() {
    gotoCard(Card.CATEGORIZATION);
  }

  public void gotoCategorizationAndShowAll() {
    categorizationSelectionView.setFilteringMode(CategorizationFilteringMode.ALL);
    gotoCategorization();
  }

  public void gotoCategorization(GlobList transactions, boolean showAllUncategorized) {
    categorizationSelectionView.show(transactions, showAllUncategorized);
    gotoCategorization();
  }

  public void gotoCategorizationForSelectedMonths() {
    categorizationSelectionView.showWithMode(CategorizationFilteringMode.SELECTED_MONTHS);
    gotoCategorization();
  }

  public void gotoUncategorizedForSelectedMonths() {
    categorizationSelectionView.showWithMode(CategorizationFilteringMode.UNCATEGORIZED_SELECTED_MONTHS);
    gotoCategorization();
  }

  public void gotoData() {
    gotoCard(Card.DATA);
  }

  public void gotoData(GlobList transactions) {
    if (transactions.isEmpty()) {
      return;
    }
    SortedSet<Integer> transactionMonthIds = transactions.getSortedSet(Transaction.MONTH);
    SortedSet<Integer> selectedMonthIds = selectionService.getSelection(Month.TYPE).getSortedSet(Month.ID);
    if (Collections.disjoint(transactionMonthIds, selectedMonthIds)) {
      ClosedMonthRange range = new ClosedMonthRange(transactionMonthIds.first(), transactionMonthIds.last());
      GlobList months = repository.getAll(Month.TYPE, GlobMatchers.fieldIn(Month.ID, range.asList()));
      selectionService.select(months, Month.TYPE);
    }
    else if (!selectedMonthIds.containsAll(transactionMonthIds)) {
      List<Integer> monthIds = new ArrayList<Integer>();
      monthIds.addAll(selectedMonthIds);
      monthIds.addAll(transactionMonthIds);
      GlobList months = repository.getAll(Month.TYPE, GlobMatchers.fieldIn(Month.ID, monthIds));
      selectionService.select(months, Month.TYPE);
    }
    transactionView.setTransactionsFilter(transactions);
    select(Card.DATA, false);
  }

  public void gotoDataWithPlannedTransactions() {
    transactionView.setPlannedTransactionsShown();
    gotoData();
  }

  public void gotoDataForAccount(Key accountKey) {
    gotoDataForAccounts(Collections.singleton(accountKey));
  }

  public void gotoDataForAccounts(Set<Key> accountKeys) {
    transactionView.setAccountFilter(accountKeys);
    select(Card.DATA, false);
  }

  public void gotoDataForSeries(Glob series) {
    selectionService.clear(Account.TYPE);
    transactionView.setSeriesFilter(series);
    select(Card.DATA, false);
  }

  public void gotoDataForSeriesGroup(Glob group) {
    selectionService.clear(Account.TYPE);
    Set<Integer> seriesIds = repository.findLinkedTo(group, Series.GROUP).getValueSet(Series.ID);
    transactionView.setSeriesFilter(seriesIds);
    select(Card.DATA, false);
  }

  public void gotoDataForSeries(Set<Integer> seriesIds) {
    selectionService.clear(Account.TYPE);
    transactionView.setSeriesFilter(seriesIds);
    select(Card.DATA, false);
  }

  public void gotoDataForAll() {
    transactionView.clearFilters();
    select(Card.DATA, false);
  }

  public void gotoAnalysisForSeries(Glob series) {
    selectionService.select(series);
    gotoCard(Card.ANALYSIS);
  }

  public void gotoProject(Glob project) {
    if (project != null) {
      selectionService.select(project);
    }
    gotoCard(Card.HOME);
  }

  public void gotoProjectItem(Glob projectItem) {
    Glob project = repository.get(Key.create(Project.TYPE, projectItem.get(ProjectItem.PROJECT)));
    if (project != null) {
      selectionService.select(
        GlobSelectionBuilder.init()
          .add(project)
          .add(projectItem)
          .get());
    }
    gotoCard(Card.PROJECTS);
  }

  public void gotoNewProject() {
    projectView.createProject();
    gotoCard(Card.PROJECTS);
  }

  public void highlightTransactionCreation() {
    select(Card.CATEGORIZATION, true);
    categorizationSelectionView.highlightTransactionCreation();
  }

  public boolean backEnabled() {
    return !backStack.isEmpty();
  }

  public boolean forwardEnabled() {
    return !forwardStack.isEmpty();
  }

  public void back() {
    forwardStack.push(currentCard);
    select(backStack.pop(), true);
  }

  public void forward() {
    backStack.push(currentCard);
    select(forwardStack.pop(), true);
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList list = selection.getAll(Card.TYPE);
    if (list.size() != 1) {
      return;
    }

    Card newCard = Card.get(list.get(0).get(Card.ID));
    if (newCard == currentCard) {
      return;
    }

    backStack.push(currentCard);
    forwardStack.clear();
    currentCard = newCard;
  }

  private void select(final Card card, boolean updateBackForward) {
    if (!updateBackForward) {
      backStack.push(currentCard);
      forwardStack.clear();
    }
    currentCard = card;
    selectionService.select(repository.get(card.getKey()));
  }

  public void reset() {
    backStack.clear();
    forwardStack.clear();
    select(Card.HOME, true);
  }
}
