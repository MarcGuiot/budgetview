package com.budgetview.gui.transactions.actions;

import com.budgetview.gui.card.NavigationService;
import com.budgetview.model.Account;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class ShowAccountTransactionsInAccountViewAction extends AbstractAction {

  private Set<Key> accounts;
  private Directory directory;

  public ShowAccountTransactionsInAccountViewAction(GlobMatcher matcher, GlobRepository repository, Directory directory) {
    super(Lang.get("showTransactionsInAccountViewAction.text"));
    this.accounts = repository.getAll(Account.TYPE, matcher).getKeySet();
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    directory.get(NavigationService.class).gotoDataForAccounts(accounts);
  }
}
