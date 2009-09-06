package org.designup.picsou.gui.accounts;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
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

public class AbstractAccountPanel<T extends GlobRepository>  {

  protected JPanel panel;
  protected T localRepository;
  protected Glob currentAccount;
  protected SelectionService selectionService;
  protected JLabel messageLabel;
  protected JTextField positionEditor;
  protected JComboBox accountTypeCombo;
  protected JLabel messageSavingsWarning;
  protected Directory localDirectory;
  private AccountTypeSelector[] accountTypeSelectors;

  public AbstractAccountPanel(T repository, Directory parentDirectory, JLabel messageLabel) {
    this.localRepository = repository;
    this.messageLabel = messageLabel;

    localDirectory = new DefaultDirectory(parentDirectory);
    selectionService = new SelectionService();
    localDirectory.add(selectionService);
  }

  protected void createComponents(GlobsPanelBuilder builder) {

    accountTypeSelectors = createTypeSelectors(localRepository);

    builder.addCombo("accountBank", Bank.TYPE)
      .setShowEmptyOption(true)
      .setEmptyOptionLabel(Lang.get("account.select.bank"))
      .setSelectionHandler(new GlobComboView.GlobSelectionHandler() {
        public void processSelection(Glob bank) {
          if (bank == null) {
            return;
          }
          GlobList entities = localRepository.findLinkedTo(bank, BankEntity.BANK);
          Glob account = AbstractAccountPanel.this.currentAccount;
          if (account != null) {
            localRepository.setTarget(account.getKey(), Account.BANK_ENTITY, entities.get(0).getKey());
          }
        }
      });
    localRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if ((currentAccount != null) && changeSet.containsChanges(currentAccount.getKey())) {
          updateCombo();
        }
      }
    });

    builder.addEditor("name", Account.NAME).setNotifyOnKeyPressed(true);
    builder.addEditor("number", Account.NUMBER).setNotifyOnKeyPressed(true);
    builder.add("type", createAccountTypeCombo());

    messageSavingsWarning = new JLabel(Lang.get("account.savings.warning"));
    builder.add("savingsMessageWarning", messageSavingsWarning);
    messageSavingsWarning.setVisible(false);

    positionEditor = builder.addEditor("position", Account.POSITION).setNotifyOnKeyPressed(true).getComponent();

    builder.addLoader(new SplitsLoader() {
      public void load(Component component, SplitsNode node) {
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

  private void updateCombo() {
    if (currentAccount == null) {
      accountTypeCombo.setSelectedIndex(-1);
    }
    else {
      for (AccountTypeSelector selector : accountTypeSelectors) {
        if (selector.isApplied(currentAccount)) {
          accountTypeCombo.setSelectedItem(selector);
        }
      }
    }
  }

  public void setBalanceEditorVisible(boolean visible) {
    positionEditor.setVisible(visible);
  }

  public void setMessageSavingsWarning(boolean visible) {
    messageSavingsWarning.setVisible(visible);
  }

  public void setMessage(String key) {
    messageLabel.setText(Lang.get(key));
  }

  public void setAccount(Glob account) {
    this.currentAccount = account;
    updateCombo();
    if (account != null) {
      selectionService.select(account);
    }
    else {
      selectionService.clear(Account.TYPE);
    }
    Glob entity = localRepository.findLinkTarget(account, Account.BANK_ENTITY);
    if (entity != null) {
      selectionService.select(localRepository.findLinkTarget(entity, BankEntity.BANK));
    }
    messageLabel.setText("");
    panel.setVisible(account != null);
  }

  public boolean check() {
    if (panel.isVisible()) {
      if (currentAccount.get(Account.BANK_ENTITY) == null) {
        setMessage("account.error.missing.bank");
        return false;
      }
      if (Strings.isNullOrEmpty(currentAccount.get(Account.NAME))) {
        setMessage("account.error.missing.name");
        return false;
      }
    }
    return true;
  }

  protected AccountTypeSelector[] createTypeSelectors(final GlobRepository repository) {
    return new AccountTypeSelector[]{
      new AccountTypeSelector("account.type.main") {
        protected void apply() {
          repository.update(currentAccount.getKey(), Account.ACCOUNT_TYPE, AccountType.MAIN.getId());
          repository.update(currentAccount.getKey(), Account.IS_CARD_ACCOUNT, false);
        }

        protected boolean isApplied(Glob account) {
          return AccountType.MAIN.getId().equals(account.get(Account.ACCOUNT_TYPE)) &&
                 !Boolean.TRUE.equals(account.get(Account.IS_CARD_ACCOUNT));
        }
      },

      new AccountTypeSelector("account.type.card") {
        protected void apply() {
          repository.update(currentAccount.getKey(), Account.ACCOUNT_TYPE, AccountType.MAIN.getId());
          repository.update(currentAccount.getKey(), Account.IS_CARD_ACCOUNT, true);
        }

        protected boolean isApplied(Glob account) {
          return AccountType.MAIN.getId().equals(account.get(Account.ACCOUNT_TYPE)) &&
                 Boolean.TRUE.equals(account.get(Account.IS_CARD_ACCOUNT));
        }
      },

      new AccountTypeSelector("account.type.savings") {
        protected void apply() {
          repository.update(currentAccount.getKey(), Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId());
          repository.update(currentAccount.getKey(), Account.IS_CARD_ACCOUNT, false);
        }

        protected boolean isApplied(Glob account) {
          return AccountType.SAVINGS.getId().equals(account.get(Account.ACCOUNT_TYPE));
        }
      },
    };
  }
}
