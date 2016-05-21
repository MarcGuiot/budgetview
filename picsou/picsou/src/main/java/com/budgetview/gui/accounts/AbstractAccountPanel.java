package com.budgetview.gui.accounts;

import com.budgetview.gui.components.tips.ErrorTip;
import com.budgetview.gui.components.tips.TipPosition;
import com.budgetview.model.Bank;
import com.budgetview.gui.bank.BankChooserDialog;
import com.budgetview.gui.components.MandatoryFieldFlag;
import com.budgetview.gui.components.TextFieldLimit;
import com.budgetview.gui.help.actions.HelpAction;
import com.budgetview.model.Account;
import com.budgetview.model.AccountCardType;
import com.budgetview.model.AccountType;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class AbstractAccountPanel<T extends GlobRepository> {

  private static final int ACCOUNT_NAME_MAX_LENGTH = 25;

  protected JPanel panel;
  protected T localRepository;
  protected Glob currentAccount;
  protected JTextField positionEditor;
  protected JTextArea warningMessage;
  protected Directory localDirectory;
  private SelectionService selectionService;
  protected AccountTypeCombo accountTypeCombo;
  protected GlobTextEditor nameField;
  private BankSelectionAction bankSelectionAction;
  private JButton bankSelectionButton;
  private ErrorTip errorTip;
  private MandatoryFieldFlag nameFlag;
  private MandatoryFieldFlag bankFlag;
  private MandatoryFieldFlag accountTypeFlag;
  private GlobTextEditor accountNumber;
  private boolean editable = true;

  private DeferredCardEditionPanel deferredPanel;

  public AbstractAccountPanel(T repository, Directory parentDirectory) {
    this.localRepository = repository;

    localDirectory = new DefaultDirectory(parentDirectory);
    selectionService = new SelectionService();
    localDirectory.add(selectionService);
    accountTypeCombo = AccountTypeCombo.create(localRepository);
  }

  protected void createComponents(GlobsPanelBuilder builder, Window dialog, final DoubleField positionField) {

    builder.add("accountTypeHelp", new HelpAction(Lang.get("account.panel.type.help"), "accountTypes",
                                                  Lang.get("help"), localDirectory));

    bankSelectionAction = new BankSelectionAction(dialog);
    bankSelectionButton = new JButton(bankSelectionAction);
    builder.add("bankSelector", bankSelectionButton);
    bankFlag = new MandatoryFieldFlag("bankFlag", builder);

    selectionService.addListener(bankSelectionAction, Account.TYPE);
    localRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (currentAccount == null) {
          return;
        }
        Key currentAccountKey = currentAccount.getKey();
        if (!repository.contains(currentAccountKey)) {
          currentAccount = null;
          accountTypeCombo.update(currentAccount);
          return;
        }
        if (changeSet.containsChanges(currentAccountKey) || changeSet.containsChanges(Bank.TYPE)) {
          accountTypeCombo.update(currentAccount);
          updateBank(currentAccount);
          updateMandatoryFlags();
          clearMessage();
          setWarning(currentAccount.get(Account.ACCOUNT_TYPE), currentAccount.get(Account.CARD_TYPE));
        }
        if (changeSet.containsChanges(currentAccountKey, Account.CARD_TYPE)) {
          deferredPanel.setVisible(currentAccount.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId()));
        }
      }
    });

    nameField = builder.addEditor("name", Account.NAME).setNotifyOnKeyPressed(true);
    nameFlag = new MandatoryFieldFlag("nameFlag", builder);
    TextFieldLimit.install(nameField.getComponent(), ACCOUNT_NAME_MAX_LENGTH);
    accountNumber = builder.addEditor("number", Account.NUMBER).setNotifyOnKeyPressed(true);
    builder.add("type", accountTypeCombo.createAccountTypeCombo());
    accountTypeFlag = new MandatoryFieldFlag("accountTypeFlag", builder);

    warningMessage = new JTextArea();
    builder.add("messageWarning", warningMessage);
    warningMessage.setVisible(false);

    deferredPanel = new DeferredCardEditionPanel(localRepository, localDirectory);
    builder.addDisposable(deferredPanel);
    builder.add("deferredPanel", deferredPanel.getPanel());

    positionEditor = builder.addEditor("position", positionField)
      .setNotifyOnKeyPressed(true).getComponent();

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
    warningMessage.setVisible(false);
    if (accountType == null) {
      return;
    }
    boolean visible = false;
    if (AccountType.MAIN.getId().equals(accountType)) {
      if (cardType == AccountCardType.CREDIT.getId()) {
        warningMessage.setText(Lang.get("account.credit.warning"));
        visible = true;
      }
      else if (cardType == AccountCardType.DEFERRED.getId()) {
        warningMessage.setText(Lang.get("account.deferred.warning"));
        visible = true;
      }
    }
    warningMessage.setVisible(visible);
  }

  public void clearMessage() {
    if (errorTip != null) {
      errorTip.dispose();
      errorTip = null;
    }
  }

  public void setAccount(Glob account) {
    this.currentAccount = account;
    accountTypeCombo.update(currentAccount);
    if (account != null) {
      selectionService.select(account);
      Integer accountType = account.get(Account.ACCOUNT_TYPE);
      setWarning(accountType, account.get(Account.CARD_TYPE));
      deferredPanel.setVisible(AccountCardType.DEFERRED.getId().equals(account.get(Account.CARD_TYPE)));
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
    updateMandatoryFlags();
  }

  public void clearMandatoryFlags() {
    nameFlag.clear();
    bankFlag.clear();
    accountTypeFlag.clear();
  }

  public void updateMandatoryFlags() {
    if (editable) {
      nameFlag.update(Strings.isNullOrEmpty(currentAccount.get(Account.NAME))
                      || localRepository.getAll(Account.TYPE, fieldEquals(Account.NAME, currentAccount.get(Account.NAME))).size() != 1);
      bankFlag.update(currentAccount.get(Account.BANK) == null);
      accountTypeFlag.update(currentAccount.get(Account.ACCOUNT_TYPE) == null);
    }
  }

  public boolean check() {
    if (panel.isVisible() && editable) {
      clearMessage();

      if (Strings.isNullOrEmpty(currentAccount.get(Account.NAME))) {
        errorTip = ErrorTip.show(nameField.getComponent(),
                                 Lang.get("account.error.missing.name"),
                                 localDirectory, TipPosition.BOTTOM_LEFT);
        accountDefinitionErrorShown();
        return false;
      }

      if (currentAccount.get(Account.NAME).length() > ACCOUNT_NAME_MAX_LENGTH) {
        errorTip = ErrorTip.show(nameField.getComponent(),
                                 Lang.get("account.error.name.too.long", ACCOUNT_NAME_MAX_LENGTH),
                                 localDirectory, TipPosition.BOTTOM_LEFT);
        accountDefinitionErrorShown();
        return false;
      }

      if (localRepository.getAll(Account.TYPE, fieldEquals(Account.NAME, currentAccount.get(Account.NAME))).size() != 1) {
        errorTip = ErrorTip.show(nameField.getComponent(),
                                 Lang.get("account.error.duplicate.name"),
                                 localDirectory, TipPosition.BOTTOM_LEFT);
        accountDefinitionErrorShown();
        return false;
      }
      if (currentAccount.get(Account.BANK) == null) {
        errorTip = ErrorTip.showLeft(bankSelectionButton, Lang.get("account.error.missing.bank"), localDirectory);
        accountDefinitionErrorShown();
        return false;
      }
      if (currentAccount.get(Account.ACCOUNT_TYPE) == null) {
        errorTip = ErrorTip.showLeft(accountTypeCombo.accountTypeCombo, Lang.get("account.error.missing.account.type"), localDirectory);
        return false;
      }
    }
    return true;
  }

  protected void accountDefinitionErrorShown() {
    nameField.getComponent().requestFocus();
  }

  public Glob getAccount() {
    return currentAccount;
  }

  public void clearAllMessages() {
    clearMessage();
    clearMandatoryFlags();
  }

  public void requestFocus() {
    nameField.getComponent().requestFocus();
  }

  private class BankSelectionAction extends AbstractAction implements GlobSelectionListener {
    private Window dialog;

    public BankSelectionAction(Window dialog) {
      this.dialog = dialog;
    }

    public void actionPerformed(ActionEvent e) {
      clearMessage();
      Key currentAccountKey = currentAccount.getKey();

      BankChooserDialog bankChooserDialog = new BankChooserDialog(dialog, localRepository, localDirectory);
      Integer bankId = bankChooserDialog.show(getCurrentBankId(),
                                              Collections.singleton(currentAccountKey.get(Account.ID)));
      if (bankId != null) {
        localRepository.update(currentAccountKey, Account.BANK, bankId);
      }
    }

    private Integer getCurrentBankId() {
      return currentAccount.get(Account.BANK);
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
      bankSelectionAction.setText(Lang.get("account.bankSelector.choose"));
    }
    else {
      Glob bank = localRepository.findLinkTarget(account, Account.BANK);
      if (bank == null) {
        bankSelectionAction.setText(Lang.get("account.bankSelector.choose"));
      }
      else {
        DescriptionService descriptionService = localDirectory.get(DescriptionService.class);
        bankSelectionAction.setText(descriptionService.getStringifier(Bank.TYPE).toString(bank, localRepository));
      }
    }
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
    this.nameField.getComponent().setEnabled(editable);
    this.accountNumber.getComponent().setEnabled(editable);
    this.accountTypeCombo.setEnabled(editable);
    this.bankSelectionButton.setEnabled(editable);
    this.positionEditor.setEnabled(editable);
    if (!editable) {
      clearMandatoryFlags();
    }
    else {
      updateMandatoryFlags();
    }
  }
}