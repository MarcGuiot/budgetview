package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.accounts.utils.AccountTypeSelector;
import org.designup.picsou.gui.accounts.utils.CardTypeEditionPanel;
import org.designup.picsou.gui.bank.BankChooserDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountCardType;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.Bank;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AbstractAccountPanel<T extends GlobRepository> {
  protected JPanel panel;
  protected T localRepository;
  protected Glob currentAccount;
  protected JLabel messageLabel;
  protected JTextField positionEditor;
  protected JComboBox accountTypeCombo;
  protected JLabel messageSavingsWarning;
  protected Directory localDirectory;
  private SelectionService selectionService;
  private AccountTypeSelector[] accountTypeSelectors;
  private CardTypeEditionPanel cardTypeEditionPanel;
  private GlobTextEditor nameField;
  private JButton accountBank;

  public AbstractAccountPanel(T repository, Directory parentDirectory, JLabel messageLabel) {
    this.localRepository = repository;
    this.messageLabel = messageLabel;

    localDirectory = new DefaultDirectory(parentDirectory);
    selectionService = new SelectionService();
    localDirectory.add(selectionService);
  }

  protected void createComponents(GlobsPanelBuilder builder, Window dialog) {

    cardTypeEditionPanel = new CardTypeEditionPanel(dialog, localRepository, localDirectory);
    accountTypeSelectors = createTypeSelectors(localRepository);

    builder.add("cardTypeEditionPanel", cardTypeEditionPanel.createComponent());

    AccountBankAction action = new AccountBankAction(dialog);
    accountBank = new JButton(action);
    builder.add("accountBank", accountBank);
    selectionService.addListener(action, Account.TYPE);
    localRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if ((currentAccount != null) && changeSet.containsChanges(currentAccount.getKey())) {
          updateAccountTypeCombo();
          updateBank(currentAccount);
        }
      }
    });

    nameField = builder.addEditor("name", Account.NAME).setNotifyOnKeyPressed(true);
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

  private void updateAccountTypeCombo() {
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
    cardTypeEditionPanel.setAccount(account);
    updateAccountTypeCombo();
    if (account != null) {
      selectionService.select(account);
    }
    else {
      selectionService.clear(Account.TYPE);
    }
    Glob bank = localRepository.findLinkTarget(account, Account.BANK);
    if (bank != null) {
      selectionService.select(bank);
    }
    messageLabel.setText("");
    panel.setVisible(account != null);
  }


  public boolean check() {
    if (panel.isVisible()) {
      if (Strings.isNullOrEmpty(currentAccount.get(Account.NAME))) {
        setMessage("account.error.missing.name");
        nameField.getComponent().requestFocus();
        return false;
      }
      if (currentAccount.get(Account.BANK) == null) {
        setMessage("account.error.missing.bank");
        return false;
      }
    }
    return true;
  }

  protected AccountTypeSelector[] createTypeSelectors(final GlobRepository repository) {
    return new AccountTypeSelector[]{
      new AccountTypeSelector("account.type.main") {
        public void apply() {
          repository.update(currentAccount.getKey(), Account.ACCOUNT_TYPE, AccountType.MAIN.getId());
          repository.update(currentAccount.getKey(), Account.CARD_TYPE, AccountCardType.NOT_A_CARD.getId());
        }

        public boolean isApplied(Glob account) {
          return AccountType.MAIN.getId().equals(account.get(Account.ACCOUNT_TYPE)) &&
                 account.get(Account.CARD_TYPE).equals(AccountCardType.NOT_A_CARD.getId());
        }
      },

      new AccountTypeSelector("accountCardType.credit") {
        public void apply() {
          repository.update(currentAccount.getKey(), Account.ACCOUNT_TYPE, AccountType.MAIN.getId());
          repository.update(currentAccount.getKey(), Account.CARD_TYPE, AccountCardType.CREDIT.getId());
        }

        public boolean isApplied(Glob account) {
          return AccountType.MAIN.getId().equals(account.get(Account.ACCOUNT_TYPE)) &&
                 Utils.equal(account.get(Account.CARD_TYPE), AccountCardType.CREDIT.getId());
        }
      },
      new AccountTypeSelector("accountCardType.deferred") {
        public void apply() {
          repository.update(currentAccount.getKey(), Account.ACCOUNT_TYPE, AccountType.MAIN.getId());
          repository.update(currentAccount.getKey(), Account.CARD_TYPE, AccountCardType.DEFERRED.getId());
        }

        public boolean isApplied(Glob account) {
          return AccountType.MAIN.getId().equals(account.get(Account.ACCOUNT_TYPE)) &&
                 Utils.equal(account.get(Account.CARD_TYPE), AccountCardType.DEFERRED.getId());
        }
      }
      ,

      new AccountTypeSelector("account.type.savings") {
        public void apply() {
          repository.update(currentAccount.getKey(), Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId());
          repository.update(currentAccount.getKey(), Account.CARD_TYPE, AccountCardType.NOT_A_CARD.getId());
        }

        public boolean isApplied(Glob account) {
          return AccountType.SAVINGS.getId().equals(account.get(Account.ACCOUNT_TYPE));
        }
      },
    };
  }

  private class AccountBankAction extends AbstractAction implements GlobSelectionListener {
    private Window dialog;

    public AccountBankAction(Window dialog) {
      this.dialog = dialog;
    }

    public void actionPerformed(ActionEvent e) {
      BankChooserDialog bankChooserDialog = new BankChooserDialog(dialog, localRepository, localDirectory);
      Integer bankId = bankChooserDialog.showBankChooser();
      if (bankId != null) {
        localRepository.update(currentAccount.getKey(), Account.BANK, bankId);
      }
    }

    public void selectionUpdated(GlobSelection selection) {
      GlobList list = selection.getAll(Account.TYPE);
      setEnabled(list.size() <= 1);
      Glob account = list.getFirst();
      updateBank(account);
    }

  }

  private void updateBank(Glob account) {
    if (account == null) {
      accountBank.setText(Lang.get("account.select.bank"));
    }
    else {
      Glob bank = localRepository.findLinkTarget(account, Account.BANK);
      if (bank == null) {
        accountBank.setText(Lang.get("account.select.bank"));
      }
      else {
        accountBank.setText(bank.get(Bank.NAME));
      }
    }
  }

}