package com.budgetview.desktop.accounts;

import com.budgetview.desktop.View;
import com.budgetview.desktop.accounts.actions.CreateAccountAction;
import com.budgetview.desktop.title.TitleView;
import com.budgetview.shared.model.AccountType;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class AccountView extends View {

  public AccountView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/accounts/accountView.splits", repository, directory);

    TitleView titleView = new TitleView(repository, directory);
    titleView.registerComponents(builder);

    MainAccountViewPanel accountViewPanel = new MainAccountViewPanel(repository, directory);
    builder.add("mainAccounts", accountViewPanel.getPanel());
    accountViewPanel.registerSignpost();

    AccountViewPanel savingsViewPanel = new SavingsAccountViewPanel(repository, directory);
    builder.add("savingsAccounts", savingsViewPanel.getPanel());

    builder.add("createAccount", new CreateAccountAction(AccountType.MAIN, repository, directory));

    parentBuilder.add("accountView", builder);
  }
}
