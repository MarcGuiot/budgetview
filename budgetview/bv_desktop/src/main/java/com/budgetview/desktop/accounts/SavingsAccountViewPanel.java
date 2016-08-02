package com.budgetview.desktop.accounts;

import com.budgetview.model.Account;
import com.budgetview.model.AccountType;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import static org.globsframework.model.utils.GlobMatchers.*;

public class SavingsAccountViewPanel extends AccountViewPanel {

  public SavingsAccountViewPanel(final GlobRepository repository, final Directory directory) {
    super(repository, directory, createMatcher(), AccountType.SAVINGS, Account.SAVINGS_SUMMARY_ACCOUNT_ID);
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
