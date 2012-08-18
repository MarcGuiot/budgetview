package org.designup.picsou.gui.transactions.actions;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
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
