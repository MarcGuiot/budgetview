package org.designup.picsou.gui.accounts;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AccountEditionPanel {
  private JPanel panel;
  private Glob account;
  protected SelectionService selectionService;
  private GlobRepository repository;
  private JLabel messageLabel;
  private GlobsPanelBuilder builder;
  private JTextField balanceEditor;
  private JComboBox accountTypeCombo;
  private AccountTypeSelector[] accountTypeSelectors = createAccountTypeSelectors();
  private JLabel messageSavingsWarning;
  private GlobLinkComboEditor updateModeCombo;

  public AccountEditionPanel(final GlobRepository repository, Directory directory, JLabel messageLabel) {
    this.repository = repository;
    this.messageLabel = messageLabel;

    Directory localDirectory = new DefaultDirectory(directory);
    selectionService = new SelectionService();
    localDirectory.add(selectionService);

    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if ((account != null) && changeSet.containsChanges(account.getKey())) {
          updateCombo();
        }
      }
    });

    createPanel(localDirectory);
  }

  private void createPanel(Directory localDirectory) {
    builder = new GlobsPanelBuilder(getClass(), "/layout/accountEditionPanel.splits",
                                    repository, localDirectory);

    builder.addCombo("accountBank", Bank.TYPE)
      .setShowEmptyOption(true)
      .setEmptyOptionLabel(Lang.get("account.select.bank"))
      .setSelectionHandler(new GlobComboView.GlobSelectionHandler() {
        public void processSelection(Glob bank) {
          if (bank == null) {
            return;
          }
          GlobList entities = repository.findLinkedTo(bank, BankEntity.BANK);
          Glob account = AccountEditionPanel.this.account;
          if (account != null) {
            repository.setTarget(account.getKey(), Account.BANK_ENTITY, entities.get(0).getKey());
          }
        }
      });
    builder.addEditor("name", Account.NAME).setNotifyOnKeyPressed(true);
    builder.addEditor("number", Account.NUMBER).setNotifyOnKeyPressed(true);
    builder.add("type", createAccountTypeCombo());
    messageSavingsWarning = new JLabel(Lang.get("account.savings.warning"));
    builder.add("savingsMessageWarning", messageSavingsWarning);
    updateModeCombo = builder.addComboEditor("updateMode", Account.UPDATE_MODE).setShowEmptyOption(false);
    messageSavingsWarning.setVisible(false);

    balanceEditor = builder.addEditor("balance", Account.BALANCE).setNotifyOnKeyPressed(true).getComponent();

    builder.addLoader(new SplitsLoader() {
      public void load(Component component) {
        panel = (JPanel)component;
        panel.setVisible(false);
      }
    });
  }

  private Component createAccountTypeCombo() {
    accountTypeCombo = new JComboBox(accountTypeSelectors);

    accountTypeCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        AccountTypeSelector selector = (AccountTypeSelector)accountTypeCombo.getSelectedItem();
        if (selector != null) {
          selector.apply();
        }
      }
    });
    return accountTypeCombo;
  }

  private AccountTypeSelector[] createAccountTypeSelectors() {
    return new AccountTypeSelector[]{
      new AccountTypeSelector("account.type.main") {
        protected void apply() {
          repository.update(account.getKey(), Account.ACCOUNT_TYPE, AccountType.MAIN.getId());
          repository.update(account.getKey(), Account.IS_CARD_ACCOUNT, false);
        }

        protected boolean isApplied(Glob account) {
          return AccountType.MAIN.getId().equals(account.get(Account.ACCOUNT_TYPE)) &&
                 !Boolean.TRUE.equals(account.get(Account.IS_CARD_ACCOUNT));
        }
      },

      new AccountTypeSelector("account.type.card") {
        protected void apply() {
          repository.update(account.getKey(), Account.ACCOUNT_TYPE, AccountType.MAIN.getId());
          repository.update(account.getKey(), Account.IS_CARD_ACCOUNT, true);
        }

        protected boolean isApplied(Glob account) {
          return AccountType.MAIN.getId().equals(account.get(Account.ACCOUNT_TYPE)) &&
                 Boolean.TRUE.equals(account.get(Account.IS_CARD_ACCOUNT));
        }
      },

      new AccountTypeSelector("account.type.savings") {
        protected void apply() {
          repository.update(account.getKey(), Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId());
          repository.update(account.getKey(), Account.IS_CARD_ACCOUNT, false);
        }

        protected boolean isApplied(Glob account) {
          return AccountType.SAVINGS.getId().equals(account.get(Account.ACCOUNT_TYPE));
        }
      },
    };
  }

  private void updateCombo() {
    if (account == null) {
      accountTypeCombo.setSelectedIndex(-1);
    }
    else {
      for (AccountTypeSelector selector : accountTypeSelectors) {
        if (selector.isApplied(account)) {
          accountTypeCombo.setSelectedItem(selector);
        }
      }
    }
  }

  public void setBalanceEditorVisible(boolean visible) {
    balanceEditor.setVisible(visible);
  }

  public void setUpdateModeEditable(boolean enabled) {
    updateModeCombo.setEnabled(enabled);
  }

  public void setMessageSavingsWarning(boolean visible) {
    messageSavingsWarning.setVisible(visible);
  }

  public GlobsPanelBuilder getBuilder() {
    return builder;
  }

  public JPanel getPanel() {
    if (panel == null) {
      builder.load();
    }
    return panel;
  }

  public void setMessage(String key) {
    messageLabel.setText(Lang.get(key));
  }

  public void setAccount(Glob account) {
    this.account = account;
    updateCombo();
    if (account != null) {
      selectionService.select(account);
    }
    else {
      selectionService.clear(Account.TYPE);
    }
    Glob entity = repository.findLinkTarget(account, Account.BANK_ENTITY);
    if (entity != null) {
      selectionService.select(repository.findLinkTarget(entity, BankEntity.BANK));
    }
    messageLabel.setText("");
    panel.setVisible(account != null);
  }

  public boolean check() {
    if (panel.isVisible()) {
      if (account.get(Account.BANK_ENTITY) == null) {
        setMessage("account.error.missing.bank");
        return false;
      }
      if (Strings.isNullOrEmpty(account.get(Account.NAME))) {
        setMessage("account.error.missing.name");
        return false;
      }
      return true;
    }
    return true;
  }

  private abstract static class AccountTypeSelector {
    private String label;

    protected AccountTypeSelector(String labelKey) {
      this.label = Lang.get(labelKey);
    }

    protected abstract void apply();

    protected abstract boolean isApplied(Glob account);

    public String toString() {
      return label;
    }
  }
}
