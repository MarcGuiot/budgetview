package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.bank.BankChooserDialog;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.help.actions.HelpAction;
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
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AbstractAccountPanel<T extends GlobRepository> {
  protected JPanel panel;
  protected T localRepository;
  protected Glob currentAccount;
  protected JTextField positionEditor;
  protected JTextArea messageWarning;
  protected Directory localDirectory;
  protected SelectionService selectionService;
  protected AccountTypeCombo accountTypeCombo;
  protected GlobTextEditor nameField;
  private JLabel accountBank;
  private AccountBankAction bankSelectionAction;
  private JButton bankSelectionButton;
  private ErrorTip errorTip;

  public AbstractAccountPanel(T repository, Directory parentDirectory) {
    this.localRepository = repository;

    localDirectory = new DefaultDirectory(parentDirectory);
    selectionService = new SelectionService();
    localDirectory.add(selectionService);
    accountTypeCombo = AccountTypeCombo.create(localRepository);
  }

  public Directory getLocalDirectory() {
    return localDirectory;
  }

  public void createComponents(GlobsPanelBuilder builder, Window dialog) {

    builder.add("accountTypeHelp", new HelpAction(Lang.get("account.panel.type.help"), "accountTypes",
                                                  Lang.get("help"), localDirectory, dialog));

    accountBank = builder.add("bankLabel", new JLabel()).getComponent();

    bankSelectionAction = new AccountBankAction(dialog);
    bankSelectionButton = new JButton(bankSelectionAction);
    builder.add("bankSelector", bankSelectionButton);
    selectionService.addListener(bankSelectionAction, Account.TYPE);
    localRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if ((currentAccount != null) && changeSet.containsChanges(currentAccount.getKey())) {
          accountTypeCombo.updateAccountTypeCombo(currentAccount);
          updateBank(currentAccount);
          check();
        }
      }
    });

    nameField = builder.addEditor("name", Account.NAME).setNotifyOnKeyPressed(true);
    builder.addEditor("number", Account.NUMBER).setNotifyOnKeyPressed(true);
    builder.add("type", accountTypeCombo.createAccountTypeCombo());

    messageWarning = new JTextArea();
    builder.add("messageWarning", messageWarning);
    messageWarning.setVisible(false);

    positionEditor = builder.addEditor("position", Account.POSITION).setNotifyOnKeyPressed(true).getComponent();

    builder.addLoader(new SplitsLoader() {
      public void load(Component component, SplitsNode node) {
        panel = (JPanel)component;
        panel.setVisible(false);
      }
    });
  }

  public void setBalanceEditorVisible(boolean visible) {
    positionEditor.setVisible(visible);
  }

  public void setWarning(Integer accountType, int cardType) {
    if (accountType == null){
      return;
    }
    boolean visible = false;
    if (AccountType.MAIN.getId().equals(accountType)) {
      if (cardType == AccountCardType.CREDIT.getId()) {
        messageWarning.setText(Lang.get("account.credit.warning"));
        visible = true;
      }
      else if (cardType == AccountCardType.DEFERRED.getId()) {
        messageWarning.setText(Lang.get("account.deferred.warning"));
        visible = true;
      }
    }
    messageWarning.setVisible(visible);
  }

  public void setSavingsWarning(boolean visible) {
    if (visible) {
      messageWarning.setText(Lang.get("account.savings.warning"));
    }
    messageWarning.setVisible(visible);
  }

  public void clearMessage() {
    if (errorTip != null) {
      errorTip.dispose();
      errorTip = null;
    }
  }

  public void setAccount(Glob account) {
    this.currentAccount = account;
    accountTypeCombo.updateAccountTypeCombo(currentAccount);
    Integer accountType = account.get(Account.ACCOUNT_TYPE);
    if (accountType != null) {
      setWarning(accountType, account.get(Account.CARD_TYPE));
    }
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
    clearMessage();
    panel.setVisible(account != null);
  }

  public boolean check() {
    if (panel.isVisible()) {
      clearMessage();
      if (Strings.isNullOrEmpty(currentAccount.get(Account.NAME))) {
        errorTip = ErrorTip.showLeft(nameField.getComponent(), Lang.get("account.error.missing.name"), localDirectory);
        nameField.getComponent().requestFocus();
        return false;
      }
      if (localRepository.getAll(Account.TYPE, GlobMatchers.fieldEquals(Account.NAME, currentAccount.get(Account.NAME))).size() != 1){
        errorTip = ErrorTip.showLeft(nameField.getComponent(), Lang.get("account.error.duplicate.name"), localDirectory);
        return false;
      }
      if (currentAccount.get(Account.BANK) == null) {
        errorTip = ErrorTip.showLeft(bankSelectionButton, Lang.get("account.error.missing.bank"), localDirectory);
        return false;
      }
      if (currentAccount.get(Account.ACCOUNT_TYPE) == null) {
        errorTip = ErrorTip.showLeft(accountTypeCombo.accountTypeCombo, Lang.get("account.error.missing.account.type"), localDirectory);
        return false;
      }
    }
    return true;
  }

  public Glob getAccount() {
    return currentAccount;
  }

  private class AccountBankAction extends AbstractAction implements GlobSelectionListener {
    private Window dialog;

    public AccountBankAction(Window dialog) {
      this.dialog = dialog;
    }

    public void actionPerformed(ActionEvent e) {
      clearMessage();
      BankChooserDialog bankChooserDialog = new BankChooserDialog(dialog, localRepository, localDirectory);
      Integer bankId = bankChooserDialog.show();
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

    public void setText(String label) {
      putValue(NAME, label);
    }
  }

  private void updateBank(Glob account) {
    if (account == null) {
      accountBank.setText("");
      bankSelectionAction.setText(Lang.get("account.bankSelector.choose"));
    }
    else {
      Glob bank = localRepository.findLinkTarget(account, Account.BANK);
      if (bank == null) {
        accountBank.setText("");
        bankSelectionAction.setText(Lang.get("account.bankSelector.choose"));
      }
      else {
        accountBank.setText(bank.get(Bank.NAME));
        bankSelectionAction.setText(Lang.get("account.bankSelector.modify"));
      }
    }
  }

}