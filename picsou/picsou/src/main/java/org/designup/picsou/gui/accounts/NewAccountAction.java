package org.designup.picsou.gui.accounts;

import org.designup.picsou.utils.Lang;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.AccountUpdateMode;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class NewAccountAction extends AbstractAction {
  private AccountType accountType;
  private GlobRepository repository;
  private Directory directory;
  private final Window owner;
  private boolean updateModeEditable = true;

  public NewAccountAction(AccountType accountType, GlobRepository repository, Directory directory, Window owner) {
    super(Lang.get("new.account"));
    this.accountType = accountType;
    this.repository = repository;
    this.directory = directory;
    this.owner = owner;
  }

  public NewAccountAction setUpdateModeEditable(boolean editable) {
    this.updateModeEditable = editable;
    return this;
  }

  public void actionPerformed(ActionEvent e) {
    AccountEditionDialog dialog = new AccountEditionDialog(owner, repository, directory);
    dialog.showWithNewAccount(accountType, AccountUpdateMode.AUTOMATIC, updateModeEditable);
  }
}
