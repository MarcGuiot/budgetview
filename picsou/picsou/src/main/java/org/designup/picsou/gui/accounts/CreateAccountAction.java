package org.designup.picsou.gui.accounts;

import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.AccountUpdateMode;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CreateAccountAction extends AbstractAction {
  private AccountType accountType;
  private GlobRepository repository;
  private Directory directory;
  private final Window owner;
  private boolean accountTypeEditable = true;
  private boolean updateModeEditable = true;

  public CreateAccountAction(AccountType accountType, GlobRepository repository, Directory directory) {
    this(accountType, repository, directory, directory.get(JFrame.class));
  }

  public CreateAccountAction(AccountType accountType, GlobRepository repository, Directory directory, Window owner) {
    super(Lang.get("account.create"));
    this.accountType = accountType;
    this.repository = repository;
    this.directory = directory;
    this.owner = owner;
    initTooltip();
  }

  public void setAccountTypeEditable(boolean accountTypeEditable) {
    this.accountTypeEditable = accountTypeEditable;
  }

  public CreateAccountAction setUpdateModeEditable(boolean editable) {
    this.updateModeEditable = editable;
    return this;
  }

  public void actionPerformed(ActionEvent e) {
    AccountEditionDialog dialog = new AccountEditionDialog(owner, repository, directory);
    dialog.showWithNewAccount(accountType, accountTypeEditable, AccountUpdateMode.AUTOMATIC, updateModeEditable);
  }

  private void initTooltip() {
    putValue(Action.SHORT_DESCRIPTION, Lang.get("newAccount." + accountType.name().toLowerCase() + ".tooltip"));
  }
}
