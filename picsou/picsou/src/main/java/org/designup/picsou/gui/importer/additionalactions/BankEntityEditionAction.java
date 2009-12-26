package org.designup.picsou.gui.importer.additionalactions;

import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobList;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;
import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.importer.AdditionalImportAction;
import org.designup.picsou.gui.importer.edition.BankEntityEditionDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.*;

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

  public boolean isValid() {
    accounts = repository.getAll(Account.TYPE)
      .filterSelf(GlobMatchers.and(GlobMatchers.isNull(Account.BANK),
                                   GlobMatchers.not(GlobMatchers.isNull(Account.BANK_ENTITY_LABEL))), repository);
    return accounts.isEmpty();
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
        BankEntityEditionDialog dialog = new BankEntityEditionDialog(repository, directory);
        dialog.show(parent, accounts);
      }
    };
  }
}