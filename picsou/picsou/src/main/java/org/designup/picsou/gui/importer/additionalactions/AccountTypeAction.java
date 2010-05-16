package org.designup.picsou.gui.importer.additionalactions;

import org.designup.picsou.gui.importer.AdditionalImportAction;
import org.designup.picsou.gui.importer.edition.BankEntityEditionDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobList;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AccountTypeAction implements AdditionalImportAction{
  private Window parent;
  private GlobRepository repository;
  private Directory directory;

  public AccountTypeAction(Window parent, GlobRepository repository, Directory directory) {
    this.parent = parent;
    this.repository = repository;
    this.directory = directory;
  }

  public boolean shouldApplyAction() {
    GlobList accounts = repository.getAll(Account.TYPE,
                                          GlobMatchers.and(GlobMatchers.isNull(Account.ACCOUNT_TYPE),
                                                           GlobMatchers.isTrue(Account.IS_VALIDADED) // pour ne pas avoir en meme temps AccountTypeChooser et ChooseOrCreateAccout
                                          ));
    return !accounts.isEmpty();
  }

  public String getMessage() {
    return Lang.get("import.account.error.missing.accountType");
  }

  public String getButtonMessage() {
    return Lang.get("import.account.mising.accountType");
  }

  public Action getAction() {
    return new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        AccountTypeChooserDialog dialog = new AccountTypeChooserDialog(parent, repository, directory);
        dialog.show();
      }
    };
  }
}
