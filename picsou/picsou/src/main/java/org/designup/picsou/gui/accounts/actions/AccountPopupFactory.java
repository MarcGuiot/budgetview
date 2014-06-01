package org.designup.picsou.gui.accounts.actions;

import org.designup.picsou.gui.accounts.AccountEditionDialog;
import org.designup.picsou.gui.accounts.AccountPositionEditionDialog;
import org.designup.picsou.gui.accounts.utils.GotoAccountWebsiteAction;
import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.actions.ToggleBooleanAction;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

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
  private Action firstAction;

  public AccountPopupFactory(Glob account, GlobRepository repository, Directory directory) {
    this.account = account;
    this.repository = repository;
    this.directory = directory;

    this.gotoWebsiteAction = new GotoAccountWebsiteAction(account, repository, directory);
    disposables.add(gotoWebsiteAction);

    this.moveUpAction = new MoveAccountUp(account.getKey(), repository);
    disposables.add(moveUpAction);

    this.moveDownAction = new MoveAccountDown(account.getKey(), repository);
    disposables.add(moveDownAction);

    this.toggleShowGraph = new ToggleBooleanAction(account.getKey(), Account.SHOW_GRAPH,
                                                   Lang.get("account.chart.hide"), Lang.get("account.chart.show"),
                                                   repository);
    disposables.add(toggleShowGraph);
  }

  public void setFirstAction(Action action) {
    this.firstAction = action;
  }

  public void setShowGraphToggle(boolean showGraphToggle) {
    this.showGraphToggle = showGraphToggle;
  }

  public JPopupMenu createPopup() {
    JPopupMenu menu = new JPopupMenu();
    if (firstAction != null) {
      menu.add(firstAction);
      menu.addSeparator();
    }
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
    menu.add(gotoWebsiteAction);
    menu.addSeparator();
    menu.add(moveUpAction);
    menu.add(moveDownAction);
    if (showGraphToggle) {
      menu.add(toggleShowGraph);
    }
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
