package org.designup.picsou.gui.accounts;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class AccountEditionPanel {
  private JPanel panel;
  private Glob account;
  protected SelectionService selectionService;
  private GlobRepository repository;
  private JLabel messageLabel;
  private GlobsPanelBuilder builder;
  private JTextField balanceEditor;

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

    balanceEditor = builder.addEditor("balance", Account.BALANCE).setNotifyOnKeyPressed(true).getComponent();

    builder.addLoader(new SplitsLoader() {
      public void load(Component component) {
        panel = (JPanel)component;
        panel.setVisible(false);
      }
    });
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
      boolean onError = Strings.isNullOrEmpty(account.get(Account.NUMBER));
      if (onError) {
        setMessage("account.error.missing.number");
      }
      return !onError;
    }
    return true;
  }
}
