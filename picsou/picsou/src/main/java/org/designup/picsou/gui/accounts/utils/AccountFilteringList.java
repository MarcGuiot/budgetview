package org.designup.picsou.gui.accounts.utils;

import org.designup.picsou.gui.components.SelectorBackgroundPainter;
import org.designup.picsou.gui.description.AccountComparator;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Account;
import org.globsframework.gui.views.GlobListView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Collections;

public class AccountFilteringList {
  private GlobListView accountFilteringList;
  private GlobRepository repository;

  public AccountFilteringList(GlobRepository repository, Directory directory,
                              GlobListView.GlobSelectionHandler handler) {
    this.repository = repository;
    accountFilteringList =
      GlobListView.init(Account.TYPE, repository, directory)
        .setShowEmptyOption(false)
        .setSingleSelectionMode();
    accountFilteringList.setRenderer(new AccountRenderer(), new AccountComparator());
    accountFilteringList.setSelectionHandler(handler);
  }

  public GlobMatcher getCurrentAccountFilter() {
    GlobList currentList = accountFilteringList.getCurrentSelection();
    if (currentList.isEmpty()) {
      return GlobMatchers.ALL;
    }
    Glob currentSelection = currentList.getFirst();
    if (!currentSelection.exists()) {
      return GlobMatchers.ALL;
    }
    Integer accountId = currentSelection.get(Account.ID);
    return Matchers.transactionsForAccounts(Collections.singleton(accountId), repository);
  }

  public JList getComponent() {
    return accountFilteringList.getComponent();
  }

  public void reset() {
    accountFilteringList.selectFirst();
  }
}
