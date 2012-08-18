package org.designup.picsou.gui.transactions.actions;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.utils.Lang;
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
