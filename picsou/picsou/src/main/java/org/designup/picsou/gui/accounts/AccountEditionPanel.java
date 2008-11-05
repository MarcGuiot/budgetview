package org.designup.picsou.gui.accounts;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class AccountEditionPanel {
  private JPanel panel;
  private Glob account;
  protected SelectionService selectionService;
  private GlobRepository repository;
  private JLabel messageLabel;
  private GlobsPanelBuilder builder;
  private JTextField balanceEditor;
  private JRadioButton saving;
  private JRadioButton daily;
  private JRadioButton creditCard;
  private JCheckBox importedCheckBox;

  public AccountEditionPanel(final GlobRepository repository, Directory directory, JLabel messageLabel) {
    this.repository = repository;
    this.messageLabel = messageLabel;

    Directory localDirectory = new DefaultDirectory(directory);
    selectionService = new SelectionService();
    localDirectory.add(selectionService);

    builder = new GlobsPanelBuilder(getClass(), "/layout/accountEditionPanel.splits",
                                    repository, localDirectory);

    builder.addCombo("accountBank", Bank.TYPE).setSelectionHandler(new GlobComboView.GlobSelectionHandler() {
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

    ButtonGroup group = new ButtonGroup();
    saving = new JRadioButton(new AbstractAction(Lang.get("account.isSaving")) {

      public void actionPerformed(ActionEvent e) {
        repository.update(account.getKey(), Account.ACCOUNT_TYPE, AccountType.SAVING.getId());
      }
    });
    daily = new JRadioButton(new AbstractAction(Lang.get("account.isDay")) {
      public void actionPerformed(ActionEvent e) {
        repository.update(account.getKey(), Account.ACCOUNT_TYPE, AccountType.DAY.getId());
      }
    });
    creditCard = new JRadioButton(new AbstractAction(Lang.get("account.isCard")) {
      public void actionPerformed(ActionEvent e) {
        repository.update(account.getKey(), Account.ACCOUNT_TYPE, AccountType.CREDIT_CARD.getId());
      }
    });
    group.add(creditCard);
    group.add(daily);
    group.add(saving);

    builder.add("savingAccount", saving);
    builder.add("defautAccount", daily);
    builder.add("cardAccount", creditCard);

    importedCheckBox = new JCheckBox(new AbstractAction(Lang.get("account.is.imported")) {
      public void actionPerformed(ActionEvent e) {
        if (account != null) {
          repository.update(account.getKey(), Account.IS_IMPORTED_ACCOUNT, importedCheckBox.isSelected());
        }
      }
    });
    builder.add("importedAccount", importedCheckBox);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (account == null) {
          return;
        }
        if (changeSet.containsChanges(account.getKey())) {
          updateRadio();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    });

    balanceEditor = builder.addEditor("balance", Account.BALANCE).setNotifyOnKeyPressed(true).getComponent();

    builder.addLoader(new SplitsLoader() {
      public void load(Component component) {
        panel = (JPanel)component;
        panel.setVisible(false);
      }
    });
  }

  private void updateRadio() {
    if (account == null) {
      creditCard.setEnabled(false);
      daily.setEnabled(false);
      saving.setEnabled(false);
      importedCheckBox.setEnabled(false);
    }
    else {
      creditCard.setEnabled(true);
      daily.setEnabled(true);
      saving.setEnabled(true);
      creditCard.setSelected(AccountType.CREDIT_CARD.getId().equals(account.get(Account.ACCOUNT_TYPE)));
      daily.setSelected(AccountType.DAY.getId().equals(account.get(Account.ACCOUNT_TYPE)));
      saving.setSelected(AccountType.SAVING.getId().equals(account.get(Account.ACCOUNT_TYPE)));
      importedCheckBox.setSelected(account.get(Account.IS_IMPORTED_ACCOUNT));
    }
  }

  public void setBalanceEditorVisible(boolean visible) {
    balanceEditor.setVisible(visible);
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
    updateRadio();
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
}
