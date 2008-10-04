package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AccountEditionDialog {
  private PicsouDialog dialog;
  private Directory directory;
  private AccountEditionPanel accountEditionPanel;
  private LocalGlobRepository localRepository;
  private Glob currentAccount;

  public AccountEditionDialog(Window owner, GlobRepository repository, Directory directory) {
    this.directory = directory;

    this.localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Bank.TYPE, BankEntity.TYPE)
      .get();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/accountEditionDialog.splits",
                                                      localRepository, directory);

    JLabel messageLabel = builder.add("message", new JLabel());

    accountEditionPanel = new AccountEditionPanel(localRepository, this.directory, messageLabel);
    builder.add("panel", accountEditionPanel.getPanel());

    dialog =
      PicsouDialog.createWithButtons(owner, builder.<JPanel>load(),
                                     new CreateAccountAction(), new CancelAction(),
                                     directory);
  }

  public void show() {
    createAccount();
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }

  private void createAccount() {
    currentAccount = localRepository.create(Account.TYPE);
    accountEditionPanel.setAccount(currentAccount);
  }

  private class CreateAccountAction extends AbstractAction {
    public CreateAccountAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      if (!accountEditionPanel.check()) {
        return;
      }
      try {
        localRepository.commitChanges(true);
        directory.get(SelectionService.class).select(currentAccount);
      }
      finally {
        dialog.setVisible(false);
      }
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }
}
