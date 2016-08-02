package com.budgetview.desktop.categorization.actions;

import com.budgetview.desktop.card.NavigationService;
import com.budgetview.desktop.transactions.actions.AbstractShowTransactionsAction;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class ShowTransactionsInCategorizationViewAction extends AbstractShowTransactionsAction {

  public ShowTransactionsInCategorizationViewAction(Set<Integer> monthIds,
                                                    GlobMatcher matcher,
                                                    GlobRepository repository, Directory directory) {
    super(Lang.get("showTransactionsInCategorizationViewAction.text"),
          monthIds, matcher, repository, directory);
  }

  protected void doShow(GlobList transactions, NavigationService navigationService) {
    navigationService.gotoCategorization(transactions, false);
  }
}
