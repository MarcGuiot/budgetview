package org.designup.picsou.gui;

import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.utils.LocalGlobRepository;
import org.crossbowlabs.globs.model.utils.LocalGlobRepositoryBuilder;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.utils.GuiUtils;
import org.crossbowlabs.splits.layout.GridBagBuilder;
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
  private AccountEditionPanel accountEditionPanel;

  public NewAccountAction(GlobRepository repository, Directory directory, Window owner) {
    super(Lang.get("new.account"));
    this.repository = repository;
    this.directory = directory;
    this.owner = owner;
  }

  public void actionPerformed(ActionEvent e) {
    final LocalGlobRepository tempRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Bank.TYPE, BankEntity.TYPE).get();

    CreateAccountAction action = new CreateAccountAction(tempRepository);
    CancelAction cancelAction = new CancelAction();
    PicsouDialog dialog = PicsouDialog.createWithButtons(owner, createEditionPanel(tempRepository), action,
                                                         cancelAction);
    action.set(dialog);
    cancelAction.set(dialog);
    createdAccount = tempRepository.create(Account.TYPE);
    accountEditionPanel.setAccount(createdAccount, null);
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }

  private JPanel createEditionPanel(LocalGlobRepository tempRepository) {
    JLabel messageLabel = new JLabel();
    accountEditionPanel = new AccountEditionPanel(tempRepository, directory, messageLabel);
    return GridBagBuilder.init()
      .add(accountEditionPanel.getPanel(), 0, 0, 1, 1, new Insets(10,10,10,10))
      .add(messageLabel, 0, 1, 1, 1, new Insets(10,10,10,10))
      .getPanel();
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
      if (!accountEditionPanel.check()) {
        return;
      }
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
