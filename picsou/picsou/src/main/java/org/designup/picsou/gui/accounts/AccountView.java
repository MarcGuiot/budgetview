package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.View;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

public class AccountView extends View {

  public AccountView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/accountView.splits", repository, directory);
    AccountViewPanel accountViewPanel = new AccountViewPanel(repository, directory,
                                                             getMatcherFor(Account.MAIN_SUMMARY_ACCOUNT_ID),
                                                             Account.MAIN_SUMMARY_ACCOUNT_ID);
    builder.add("mainAccount", accountViewPanel.getPanel());
    AccountViewPanel savingsViewPanel = new AccountViewPanel(repository, directory,
                                                             getMatcherFor(Account.SAVINGS_SUMMARY_ACCOUNT_ID),
                                                             Account.SAVINGS_SUMMARY_ACCOUNT_ID);
    builder.add("savingsAccount", savingsViewPanel.getPanel());
    parentBuilder.add("accountView", builder);
  }

  private GlobMatcher getMatcherFor(int summaryId) {
    Integer accountType;
    if (summaryId == Account.MAIN_SUMMARY_ACCOUNT_ID) {
      accountType = AccountType.MAIN.getId();
    }
    else {
      accountType = AccountType.SAVINGS.getId();
    }
    return GlobMatchers.and(GlobMatchers.fieldEquals(Account.ACCOUNT_TYPE, accountType),
                            GlobMatchers.not(GlobMatchers.fieldEquals(Account.ID, summaryId)),
                            GlobMatchers.not(GlobMatchers.fieldEquals(Account.ID, Account.ALL_SUMMARY_ACCOUNT_ID)));
  }

}
