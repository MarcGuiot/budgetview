package com.budgetview.desktop.accounts.actions;

import com.budgetview.desktop.accounts.AccountEditionDialog;
import com.budgetview.desktop.card.NavigationService;
import com.budgetview.shared.model.AccountType;
import com.budgetview.model.AccountUpdateMode;
import com.budgetview.utils.Lang;
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
  private boolean gotoAccountView = false;

  public CreateAccountAction(AccountType accountType, GlobRepository repository, Directory directory) {
    this(accountType, repository, directory, directory.get(JFrame.class));
  }

  public CreateAccountAction(AccountType accountType, GlobRepository repository, Directory directory, Window owner) {
    this("account.create", accountType, repository,  directory, owner);
  }

  public CreateAccountAction(String labelKey, AccountType accountType, GlobRepository repository, Directory directory, Window owner) {
    super(Lang.get(labelKey));
    this.accountType = accountType;
    this.repository = repository;
    this.directory = directory;
    this.owner = owner;
    initTooltip();
  }

  public void setAccountTypeEditable(boolean accountTypeEditable) {
    this.accountTypeEditable = accountTypeEditable;
  }

  public void setGotoAccountViewEnabled(boolean enabled) {
    this.gotoAccountView = enabled;
  }

  public void actionPerformed(ActionEvent e) {
    if (gotoAccountView) {
      directory.get(NavigationService.class).gotoData();
    }

    AccountEditionDialog dialog = new AccountEditionDialog(owner, repository, directory, true);
    dialog.showWithNewAccount(accountType, accountTypeEditable, AccountUpdateMode.AUTOMATIC);
  }

  private void initTooltip() {
    putValue(Action.SHORT_DESCRIPTION, Lang.get("newAccount." + accountType.name().toLowerCase() + ".tooltip"));
  }

}
