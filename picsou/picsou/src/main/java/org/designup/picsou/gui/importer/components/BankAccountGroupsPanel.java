package org.designup.picsou.gui.importer.components;

import org.designup.picsou.gui.browsing.BrowsingAction;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public class BankAccountGroupsPanel {

  private GlobRepository repository;
  private Directory directory;
  private final boolean showWebSiteLink;
  private Repeat<BankAccountGroup> synchroAccountsRepeat;
  private JComponent component;
  private GlobsPanelBuilder builder;

  public BankAccountGroupsPanel(boolean showWebSiteLink, GlobRepository repository, Directory directory) {
    this.showWebSiteLink = showWebSiteLink;
    this.repository = repository;
    this.directory = directory;
    createPanel();
  }

  private void createPanel() {
    builder = new GlobsPanelBuilder(getClass(),
                                    "/layout/importexport/components/bankAccountGroupsPanel.splits",
                                    repository, directory);

    synchroAccountsRepeat =
      builder.addRepeat("accountGroups", Collections.<BankAccountGroup>emptyList(), new SynchroAccountRepeatFactory());

    component = builder.load();
  }

  public JComponent getComponent() {
    return component;
  }

  public void update(List<BankAccountGroup> accountGroups) {
    synchroAccountsRepeat.set(accountGroups);
  }

  public void dispose() {
    builder.dispose();
  }

  private class SynchroAccountRepeatFactory implements RepeatComponentFactory<BankAccountGroup> {
    public void registerComponents(RepeatCellBuilder groupCellBuilder, final BankAccountGroup accountsGroup) {
      groupCellBuilder.add("bankLabel", createBankLabel(accountsGroup));

      groupCellBuilder.addRepeat("accounts", accountsGroup.getAccounts(), new RepeatComponentFactory<Glob>() {
        public void registerComponents(RepeatCellBuilder cellBuilder, Glob account) {
          cellBuilder.add("accountLabel", new JLabel(account.get(Account.NAME)));
        }
      });

      Action action = new BrowsingAction(Lang.get("bankAccountsGroup.gotoWebsite"), directory) {
        protected String getUrl() {
          return accountsGroup.getBank().get(Bank.URL);
        }
      };
      JButton gotoWebsiteButton = new JButton(action);
      groupCellBuilder.add("gotoWebsite", gotoWebsiteButton);
      gotoWebsiteButton.setEnabled(showWebSiteLink);
      gotoWebsiteButton.setVisible(showWebSiteLink);
    }

    private JLabel createBankLabel(BankAccountGroup accountsGroup) {
      JLabel bankLabel = new JLabel();

      String shortName = accountsGroup.getBank().get(Bank.SHORT_NAME);
      if (Strings.isNotEmpty(shortName)) {
        bankLabel.setText(shortName);
        return bankLabel;
      }

      String name = accountsGroup.getBank().get(Bank.NAME);
      bankLabel.setText(Strings.cut(name, 30));
      return bankLabel;
    }
  }
}
