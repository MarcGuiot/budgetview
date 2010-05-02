package org.designup.picsou.gui.accounts.utils;

import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class GotoAccountOperationsAction extends AbstractAction {
  private Key accountKey;
  private NavigationService navigationService;

  public GotoAccountOperationsAction(Glob account, GlobRepository repository, Directory directory) {
    super(Lang.get("accountView.goto.operations"));
    putValue(Action.SHORT_DESCRIPTION, Lang.get("accountView.goto.operations.tooltip"));
    this.navigationService = directory.get(NavigationService.class);
    this.accountKey = account.getKey();
  }

  public void actionPerformed(ActionEvent e) {
    navigationService.gotoDataForAccount(accountKey);
  }
}