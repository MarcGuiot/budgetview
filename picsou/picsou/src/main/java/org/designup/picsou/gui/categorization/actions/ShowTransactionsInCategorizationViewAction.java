package org.designup.picsou.gui.categorization.actions;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.transactions.actions.AbstractShowTransactionsAction;
import org.designup.picsou.utils.Lang;
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
