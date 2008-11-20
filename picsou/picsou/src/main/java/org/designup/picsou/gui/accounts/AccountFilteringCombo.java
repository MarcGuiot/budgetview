package org.designup.picsou.gui.accounts;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.gui.description.AccountComparator;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import static org.globsframework.model.utils.GlobMatchers.ALL;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class AccountFilteringCombo {
  private GlobComboView accountFilteringCombo;

  public AccountFilteringCombo(GlobRepository repository, Directory directory,
                               GlobComboView.GlobSelectionHandler handler) {
    accountFilteringCombo = GlobComboView.init(Account.TYPE, repository, directory).setShowEmptyOption(false);
    accountFilteringCombo.setRenderer(new AccountRenderer(), new AccountComparator());
    accountFilteringCombo.setSelectionHandler(handler);
  }

  public GlobMatcher getCurrentAccountFilter() {
    Integer accountId = accountFilteringCombo.getCurrentSelection().get(Account.ID);
    if (accountId.equals(Account.SUMMARY_ACCOUNT_ID)) {
      return ALL;
    }
    return fieldEquals(Transaction.ACCOUNT, accountId);
  }

  public JComboBox getComponent() {
    return accountFilteringCombo.getComponent();
  }
}