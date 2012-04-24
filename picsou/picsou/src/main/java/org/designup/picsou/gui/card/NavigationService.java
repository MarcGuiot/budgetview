package org.designup.picsou.gui.card;

import org.designup.picsou.gui.budget.BudgetToggle;
import org.designup.picsou.gui.categorization.CategorizationView;
import org.designup.picsou.gui.categorization.components.CategorizationFilteringMode;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.Account;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import java.util.Stack;

public class NavigationService implements GlobSelectionListener {

  public static final Card INITIAL_CARD = Card.HOME;

  private SelectionService selectionService;
  private TransactionView transactionView;
  private CategorizationView categorizationView;
  private BudgetToggle budgetToggle;
  private GlobRepository repository;

  private Card currentCard = INITIAL_CARD;
  private Stack<Card> backStack = new Stack<Card>();
  private Stack<Card> forwardStack = new Stack<Card>();

  public NavigationService(TransactionView transactionView,
                           CategorizationView categorizationView,
                           BudgetToggle budgetToggle,
                           GlobRepository repository,
                           Directory directory) {
    this.transactionView = transactionView;
    this.categorizationView = categorizationView;
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
    categorizationView.setFilteringMode(CategorizationFilteringMode.ALL);
    gotoCategorization();
  }

  public void gotoCategorization(GlobList transactions, boolean showAllUncategorized) {
    categorizationView.show(transactions, showAllUncategorized);
    gotoCategorization();
  }

  public void gotoUncategorizedForSelectedMonths() {
    categorizationView.showUncategorizedForSelectedMonths();
    gotoCategorization();
  }

  public void gotoData() {
    gotoCard(Card.DATA);
  }

  public void gotoDataWithPlannedTransactions() {
    transactionView.setPlannedTransactionsShown();
    gotoData();
  }

  public void gotoDataForAccount(Key accountKey) {
    selectionService.select(repository.get(accountKey));
    transactionView.setAccountFilter(accountKey);
    select(Card.DATA, false);
  }

  public void gotoDataForSeries(Glob series) {
    selectionService.clear(Account.TYPE);
    transactionView.setSeriesFilter(series);
    select(Card.DATA, false);
  }

  public void gotoAnalysisForSeries(Glob series) {
    selectionService.select(series);
    gotoCard(Card.ANALYSIS);
  }

  public void highlightTransactionCreation() {
    select(Card.CATEGORIZATION, true);
    categorizationView.highlightTransactionCreation();
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
