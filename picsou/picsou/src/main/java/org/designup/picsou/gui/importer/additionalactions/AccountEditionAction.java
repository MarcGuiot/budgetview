package org.designup.picsou.gui.importer.additionalactions;

import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.GlobList;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.utils.directory.Directory;
import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.accounts.AccountEditionDialog;
import org.designup.picsou.gui.importer.AdditionalImportAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AccountEditionAction implements AdditionalImportAction {
  private Window parent;
  private GlobRepository repository;
  private Directory directory;

  public AccountEditionAction(Window parent, GlobRepository repository, Directory directory) {
    this.parent = parent;
    this.repository = repository;
    this.directory = directory;
  }

  public boolean isValid() {
    GlobList accounts = repository.getAll(Account.TYPE);
    for (Integer accountId : Account.SUMMARY_ACCOUNT_IDS) {
      accounts.remove(repository.get(Key.create(Account.TYPE, accountId)));
    }
    return !accounts.isEmpty();
  }

  public String getMessage() {
    return Lang.get("import.account.none");
  }

  public String getButtonMessage(){
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
          dialog.createAndShow();
        }
      }
    };
  }
}
