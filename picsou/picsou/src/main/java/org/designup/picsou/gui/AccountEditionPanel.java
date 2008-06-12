package org.designup.picsou.gui;

import org.crossbowlabs.globs.gui.GlobsPanelBuilder;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.gui.views.GlobComboView;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.Strings;
import org.crossbowlabs.globs.utils.directory.DefaultDirectory;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.utils.Lang;

import javax.swing.*;

public class AccountEditionPanel {
  private JPanel panel;
  private Glob account;
  protected SelectionService selectionService;
  private GlobRepository repository;
  private JLabel messageLabel;

  public AccountEditionPanel(final GlobRepository repository, Directory directory, JLabel messageLabel) {
    this.repository = repository;
    this.messageLabel = messageLabel;

    Directory localDirectory = new DefaultDirectory(directory);
    selectionService = new SelectionService();
    localDirectory.add(selectionService);

    GlobsPanelBuilder builder = GlobsPanelBuilder.init(repository, localDirectory);
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
    builder.addEditor(Account.NAME);
    builder.addEditor(Account.NUMBER);

    panel = (JPanel)builder.parse(getClass(), "/layout/accountEditionPanel.splits");
    panel.setVisible(false);
  }

  public void setMessage(String key) {
    messageLabel.setText(Lang.get(key));
  }

  public void setAccount(Glob account, Glob bank) {
    this.account = account;
    if (account != null) {
      selectionService.select(account);
    }
    else {
      selectionService.clear(Account.TYPE);
    }
    if (bank == null) {
      Glob entity = repository.findLinkTarget(account, Account.BANK_ENTITY);
      if (entity != null) {
        selectionService.select(repository.findLinkTarget(entity, BankEntity.BANK));
      }
    }
    else {
      selectionService.select(bank);
    }
    messageLabel.setText("");
    panel.setVisible(account != null);
  }

  public JPanel getPanel() {
    return panel;
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
