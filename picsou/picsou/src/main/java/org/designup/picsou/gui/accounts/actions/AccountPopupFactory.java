package org.designup.picsou.gui.accounts.actions;

import org.designup.picsou.gui.accounts.AccountEditionDialog;
import org.designup.picsou.gui.accounts.AccountPositionEditionDialog;
import org.designup.picsou.gui.accounts.utils.GotoAccountWebsiteAction;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.ProjectAccountGraph;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.actions.ToggleBooleanAction;
import org.globsframework.gui.actions.ToggleSelectionAction;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AccountPopupFactory implements PopupMenuFactory, Disposable {
  private final Glob account;
  private final GlobRepository repository;
  private final Directory directory;
  private DisposableGroup disposables = new DisposableGroup();
  private GotoAccountWebsiteAction gotoWebsiteAction;
  private final MoveAccountUp moveUpAction;
  private final MoveAccountDown moveDownAction;
  private final ToggleBooleanAction toggleShowGraph;
  private boolean showGraphToggle = true;
  private boolean showSelectionToggle = false;
  private final ToggleSelectionAction selectionAction;

  public AccountPopupFactory(Glob account, GlobRepository repository, Directory directory, boolean isForProjects) {
    this.account = account;
    this.repository = repository;
    this.directory = directory;
    Key accountKey = account.getKey();

    this.selectionAction = new ToggleSelectionAction(accountKey,
                                                     Lang.get("account.action.select"),
                                                     Lang.get("account.action.unselect"),
                                                     repository, directory);
    disposables.add(selectionAction);

    this.gotoWebsiteAction = new GotoAccountWebsiteAction(account, repository, directory);
    disposables.add(gotoWebsiteAction);

    this.moveUpAction = new MoveAccountUp(accountKey, repository);
    disposables.add(moveUpAction);

    this.moveDownAction = new MoveAccountDown(accountKey, repository);
    disposables.add(moveDownAction);

    if (isForProjects) {
      this.toggleShowGraph = new ToggleBooleanAction(Key.create(ProjectAccountGraph.TYPE, accountKey.get(Account.ID)),
                                                     ProjectAccountGraph.SHOW,
                                                     Lang.get("account.chart.hide"), Lang.get("account.chart.show"),
                                                     repository);
    }
    else {
      this.toggleShowGraph = new ToggleBooleanAction(accountKey, Account.SHOW_CHART,
                                                     Lang.get("account.chart.hide"), Lang.get("account.chart.show"),
                                                     repository);
    }

    disposables.add(toggleShowGraph);
  }

  public void setShowGraphToggle(boolean show) {
    this.showGraphToggle = show;
  }

  public void setShowSelectionToggle(boolean show) {
    this.showSelectionToggle = show;
  }

  public JPopupMenu createPopup() {
    JPopupMenu menu = new JPopupMenu();

    menu.add(new AbstractAction(Lang.get("accountView.edit")) {
      public void actionPerformed(ActionEvent e) {
        AccountEditionDialog dialog = new AccountEditionDialog(repository, directory, false);
        dialog.show(account.getKey());
      }
    });
    menu.add(new AbstractAction(Lang.get("accountView.editPosition")) {
      public void actionPerformed(ActionEvent e) {
        editPosition();
      }
    });
    menu.addSeparator();
    if (showSelectionToggle) {
      menu.add(selectionAction);
      menu.addSeparator();
    }
    menu.add(moveUpAction);
    menu.add(moveDownAction);
    if (showGraphToggle) {
      menu.add(toggleShowGraph);
    }
    menu.addSeparator();
    menu.add(gotoWebsiteAction);
    menu.addSeparator();
    menu.add(new DeleteAccountAction(account, repository, directory));
    return menu;
  }

  public void editPosition() {
    AccountPositionEditionDialog accountPositionEditor =
      new AccountPositionEditionDialog(account, repository, directory, directory.get(JFrame.class));
    accountPositionEditor.show();
  }

  public void dispose() {
    disposables.dispose();
  }
}
