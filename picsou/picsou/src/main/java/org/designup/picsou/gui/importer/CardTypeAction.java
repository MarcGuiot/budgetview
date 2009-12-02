package org.designup.picsou.gui.importer;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountCardType;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CardTypeAction implements AdditionalImportAction {
  private Window parent;
  private GlobRepository repository;
  private Directory directory;
  private GlobList accounts;

  public CardTypeAction(Window parent, GlobRepository repository, Directory directory) {
    this.parent = parent;
    this.repository = repository;
    this.directory = directory;
  }

  public boolean isValid() {
    accounts = repository.getAll(Account.TYPE)
      .filterSelf(GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.UNDEFINED.getId()), repository);
    return accounts.isEmpty();
  }

  public String getMessage() {
    return Lang.get("account.error.missing.cardType.message");
  }

  public String getButtonMessage() {
    return Lang.get("account.error.missing.cardType.button");
  }

  public Action getAction() {
    return new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        CartTypeChooserDialog chooserDialog = new CartTypeChooserDialog(repository, directory);
        chooserDialog.show(parent, accounts);
      }
    };
  }
}
