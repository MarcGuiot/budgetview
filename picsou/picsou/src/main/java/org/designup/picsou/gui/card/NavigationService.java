package org.designup.picsou.gui.card;

import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.gui.categorization.CategorizationView;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobList;
import org.globsframework.utils.directory.Directory;

public class NavigationService {
  private SelectionService selectionService;
  private TransactionView transactionView;
  private CategorizationView categorizationView;
  private GlobRepository repository;

  public NavigationService(TransactionView transactionView, CategorizationView categorizationView, GlobRepository repository, Directory directory) {
    this.transactionView = transactionView;
    this.categorizationView = categorizationView;
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);
  }

  public void gotoHome() {
    select(Card.HOME);
  }

  public void gotoCategorization() {
    select(Card.CATEGORIZATION);
  }

  public void gotoCategorization(GlobList transactions) {
    categorizationView.show(transactions);
    gotoCategorization();
  }

  public void gotoData() {
    select(Card.DATA);
  }

  private void select(final Card card) {
    selectionService.select(repository.get(card.getKey()));
  }
}
