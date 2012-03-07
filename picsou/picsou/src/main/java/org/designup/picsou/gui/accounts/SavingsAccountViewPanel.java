package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.accounts.position.AccountPositionLabels;
import org.designup.picsou.gui.accounts.position.SavingsAccountPositionLabels;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

public class SavingsAccountViewPanel extends AccountViewPanel {

  public SavingsAccountViewPanel(final GlobRepository repository, final Directory directory) {
    super(repository, directory, createMatcher(), Account.SAVINGS_SUMMARY_ACCOUNT_ID);
  }

  private static GlobMatcher createMatcher() {
    return and(fieldEquals(Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId()),
               not(fieldEquals(Account.ID, Account.SAVINGS_SUMMARY_ACCOUNT_ID)),
               not(fieldEquals(Account.ID, Account.EXTERNAL_ACCOUNT_ID)),
               not(fieldEquals(Account.ID, Account.ALL_SUMMARY_ACCOUNT_ID)));
  }

  protected AccountType getAccountType() {
    return AccountType.SAVINGS;
  }
}
