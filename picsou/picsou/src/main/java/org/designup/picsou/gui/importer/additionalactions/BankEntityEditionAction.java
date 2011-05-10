package org.designup.picsou.gui.importer.additionalactions;

import org.designup.picsou.gui.importer.AdditionalImportAction;
import org.designup.picsou.gui.importer.edition.BankEntityEditionDialog;
import org.designup.picsou.gui.bank.BankChooserDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class BankEntityEditionAction implements AdditionalImportAction {
  private Window parent;
  private GlobRepository repository;
  private Directory directory;
  private GlobList accounts;

  public BankEntityEditionAction(Window parent, GlobRepository repository, Directory directory) {
    this.parent = parent;
    this.repository = repository;
    this.directory = directory;
  }

  public boolean shouldApplyAction() {
    accounts = repository.getAll(Account.TYPE)
      .filterSelf(GlobMatchers.and(GlobMatchers.isNull(Account.BANK),
                                   GlobMatchers.not(GlobMatchers.isNull(Account.BANK_ENTITY_LABEL))), repository);
    return !accounts.isEmpty();
  }

  public String getMessage() {
    return Lang.get("account.error.missing.bank");
  }

  public String getButtonMessage() {
    return Lang.get("import.account.bankentity");
  }

  public Action getAction() {
    return new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if (accounts.size() == 1) {
          BankChooserDialog dialog = new BankChooserDialog(parent, repository, directory);
          Integer bankId = dialog.show();
          if (bankId != null){
            repository.update(accounts.get(0).getKey(), Account.BANK, bankId);
          }
        }
        else {
          BankEntityEditionDialog dialog = new BankEntityEditionDialog(repository, directory);
          dialog.show(parent, accounts);
        }
      }
    };
  }
}
