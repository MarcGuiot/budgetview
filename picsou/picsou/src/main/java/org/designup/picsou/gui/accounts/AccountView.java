package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.actions.CreateAccountAction;
import org.designup.picsou.gui.components.layoutconfig.SplitPaneConfig;
import org.designup.picsou.gui.title.TitleView;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.LayoutConfig;
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

    AccountViewPanel accountViewPanel = new MainAccountViewPanel(repository, directory);
    builder.add("mainAccounts", accountViewPanel.getPanel());

    AccountViewPanel savingsViewPanel = new SavingsAccountViewPanel(repository, directory);
    builder.add("savingsAccounts", savingsViewPanel.getPanel());

    builder.add("createAccount", new CreateAccountAction(AccountType.MAIN, repository, directory));

    parentBuilder.add("accountView", builder);
  }
}
