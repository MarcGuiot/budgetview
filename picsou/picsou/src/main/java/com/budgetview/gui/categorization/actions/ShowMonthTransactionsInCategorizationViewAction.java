package com.budgetview.gui.categorization.actions;

import com.budgetview.gui.card.NavigationService;
import com.budgetview.gui.transactions.actions.AbstractShowTransactionsAction;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class ShowMonthTransactionsInCategorizationViewAction extends AbstractShowTransactionsAction {

  public ShowMonthTransactionsInCategorizationViewAction(Set<Integer> monthIds,
                                                         GlobRepository repository, Directory directory) {
    super(Lang.get("showTransactionsInCategorizationViewAction.text"),
          monthIds, GlobMatchers.ALL, repository, directory);
  }

  protected void doShow(GlobList transactions, NavigationService navigationService) {
    navigationService.gotoCategorizationForSelectedMonths();
  }
}
