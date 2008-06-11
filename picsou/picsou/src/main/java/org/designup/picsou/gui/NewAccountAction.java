package org.designup.picsou.gui;

import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.utils.LocalGlobRepository;
import org.crossbowlabs.globs.model.utils.LocalGlobRepositoryBuilder;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.utils.GuiUtils;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class NewAccountAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;
  private final Window owner;
  private Glob createdAccount;

  public NewAccountAction(GlobRepository repository, Directory directory, Window owner) {
    super(Lang.get("new.account"));
    this.repository = repository;
    this.directory = directory;
    this.owner = owner;
  }

  public void actionPerformed(ActionEvent e) {
    final LocalGlobRepository tempRespository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Bank.TYPE, BankEntity.TYPE).get();
    AccountEditionPanel accountEditionPanel = new AccountEditionPanel(tempRespository, directory);
    CreateAccountAction action = new CreateAccountAction(tempRespository);
    CancelAction cancelAction = new CancelAction();
    PicsouDialog dialog = PicsouDialog.createWithButtons(owner, accountEditionPanel.getPanel(), action,
                                                         cancelAction);
    action.set(dialog);
    cancelAction.set(dialog);
    createdAccount = tempRespository.create(Account.TYPE);
    accountEditionPanel.setAccount(createdAccount, null);
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }

  private class CancelAction extends AbstractAction {
    private PicsouDialog dialog;

    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }

    public void set(PicsouDialog dialog) {
      this.dialog = dialog;
    }
  }


  private class CreateAccountAction extends AbstractAction {
    private final LocalGlobRepository tempRespository;
    private PicsouDialog dialog;

    public CreateAccountAction(LocalGlobRepository tempRespository) {
      super(Lang.get("ok"));
      this.tempRespository = tempRespository;
    }

    public void actionPerformed(ActionEvent e) {
      try {
        tempRespository.commitChanges(true);
        directory.get(SelectionService.class).select(createdAccount);
      }
      finally {
        dialog.setVisible(false);
      }
    }

    public void set(PicsouDialog dialog) {
      this.dialog = dialog;
    }
  }
}
