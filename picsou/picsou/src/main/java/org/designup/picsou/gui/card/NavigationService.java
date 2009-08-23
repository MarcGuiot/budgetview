package org.designup.picsou.gui.card;

import org.designup.picsou.gui.categorization.CategorizationView;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.series.view.SeriesView;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
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
  private CategorizationView categorizationView;
  private SeriesView seriesView;
  private GlobRepository repository;

  private Card currentCard = INITIAL_CARD;
  private Stack<Card> backStack = new Stack<Card>();
  private Stack<Card> forwardStack = new Stack<Card>();

  public NavigationService(CategorizationView categorizationView,
                           SeriesView seriesView,
                           GlobRepository repository,
                           Directory directory) {
    this.categorizationView = categorizationView;
    this.seriesView = seriesView;
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

  public void gotoCategorization() {
    gotoCard(Card.CATEGORIZATION);
  }

  public void gotoCategorization(GlobList transactions) {
    categorizationView.show(transactions);
    gotoCategorization();
  }

  public void gotoDataForSeries(Glob series) {
    selectionService.select(repository.get(Account.ALL_SUMMARY_KEY));
    seriesView.selectSeries(series);
    select(Card.DATA, false);
  }

  public void gotoData(BudgetArea budgetArea) {
    selectionService.select(repository.get(Account.ALL_SUMMARY_KEY));
    seriesView.selectBudgetArea(budgetArea);
    select(Card.DATA, false);
  }

  public void gotoDataForSavingsAccount(Integer accountId) {
    selectionService.select(repository.get(Key.create(Account.TYPE, accountId)));
    select(Card.DATA, false);
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

  private void select(final Card card, boolean backForward) {
    if (!backForward) {
      backStack.push(currentCard);
      forwardStack.clear();
    }
    currentCard = card;
    selectionService.select(repository.get(card.getKey()));
  }

  public void reset(){
    backStack.clear();
    forwardStack.clear();
    select(Card.HOME, true);
  }
}
