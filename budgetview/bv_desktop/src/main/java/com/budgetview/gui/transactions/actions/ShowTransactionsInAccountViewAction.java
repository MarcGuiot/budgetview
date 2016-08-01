package com.budgetview.gui.transactions.actions;

import com.budgetview.gui.card.NavigationService;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class ShowTransactionsInAccountViewAction extends AbstractShowTransactionsAction {

  public ShowTransactionsInAccountViewAction(Set<Integer> monthIds, GlobMatcher matcher, GlobRepository repository,
                                             Directory directory) {
    super(Lang.get("showTransactionsInAccountViewAction.text"),
          monthIds, matcher, repository, directory);
  }

  protected void doShow(GlobList transactions, NavigationService navigationService) {
    navigationService.gotoData(transactions);
  }
}
