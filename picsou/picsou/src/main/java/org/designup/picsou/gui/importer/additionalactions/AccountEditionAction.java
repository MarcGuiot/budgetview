package org.designup.picsou.gui.importer.additionalactions;

import org.designup.picsou.gui.accounts.AccountEditionDialog;
import org.designup.picsou.gui.importer.AdditionalImportAction;
import org.designup.picsou.gui.model.CurrentAccountInfo;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.FieldValue.value;

public class AccountEditionAction implements AdditionalImportAction {
  private Window parent;
  private GlobRepository repository;
  private Directory directory;

  public AccountEditionAction(Window parent, GlobRepository repository, Directory directory) {
    this.parent = parent;
    this.repository = repository;
    this.directory = directory;
  }

  public boolean shouldApplyAction() {
    GlobList accounts = repository.getAll(Account.TYPE);
    for (Integer accountId : Account.SUMMARY_ACCOUNT_IDS) {
      accounts.remove(repository.get(Key.create(Account.TYPE, accountId)));
    }
    return accounts.isEmpty();
  }

  public String getMessage() {
    return Lang.get("import.account.none");
  }

  public String getButtonMessage() {
    return Lang.get("import.account.button");
  }

  public Action getAction() {
    return new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        GlobList accounts = repository.getAll(Account.TYPE);
        for (Integer accountId : Account.SUMMARY_ACCOUNT_IDS) {
          accounts.remove(repository.get(Key.create(Account.TYPE, accountId)));
        }
        if (accounts.size() == 0) {
          AccountEditionDialog dialog = new AccountEditionDialog(parent, repository, directory);
          Glob accountInfo = repository.find(Key.create(CurrentAccountInfo.TYPE, 0));

          if (accountInfo != null){
            dialog.setAccountInfo(accountInfo);
          }
          dialog.showWithNewAccount(value(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()));
        }
      }
    };
  }
}
