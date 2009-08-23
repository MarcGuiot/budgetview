package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.description.AccountComparator;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.model.Account;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Collections;

public class AccountFilteringCombo {
  private GlobComboView accountFilteringCombo;
  private GlobRepository repository;

  public AccountFilteringCombo(GlobRepository repository, Directory directory,
                               GlobComboView.GlobSelectionHandler handler) {
    this.repository = repository;
    accountFilteringCombo = GlobComboView.init(Account.TYPE, repository, directory).setShowEmptyOption(false);
    accountFilteringCombo.setRenderer(new AccountRenderer(), new AccountComparator());
    accountFilteringCombo.setSelectionHandler(handler);
  }

  public GlobMatcher getCurrentAccountFilter() {
    Glob currentSelection = accountFilteringCombo.getCurrentSelection();
    if (currentSelection == null || !currentSelection.exists()) {
      return GlobMatchers.ALL;
    }
    Integer accountId = currentSelection.get(Account.ID);
    return PicsouMatchers.transactionsForAccounts(Collections.singleton(accountId), repository);
  }

  public JComboBox getComponent() {
    return accountFilteringCombo.getComponent();
  }

  public void reset(){
    accountFilteringCombo.selectFirst();
  }
}
