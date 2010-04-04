package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.View;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class AccountView extends View {

  public AccountView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/accounts/accountView.splits", repository, directory);

    AccountViewPanel accountViewPanel = new MainAccountViewPanel(repository, directory);
    builder.add("mainAccount", accountViewPanel.getPanel());

    AccountViewPanel savingsViewPanel = new SavingsAccountViewPanel(repository, directory);
    builder.add("savingsAccount", savingsViewPanel.getPanel());

    parentBuilder.add("accountView", builder);
  }
}
