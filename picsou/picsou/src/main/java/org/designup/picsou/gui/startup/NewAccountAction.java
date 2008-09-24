package org.designup.picsou.gui.startup;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

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
    PicsouDialog dialog =
      PicsouDialog.createWithButtons(owner.getOwner(), createEditionPanel(tempRepository),
                                     action,
                                     cancelAction, directory);
    action.set(dialog);
    cancelAction.set(dialog);
    createdAccount = tempRepository.create(Account.TYPE);
    accountEditionPanel.setAccount(createdAccount);
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }

  private JPanel createEditionPanel(LocalGlobRepository tempRepository) {
    JLabel messageLabel = new JLabel();
    accountEditionPanel = new AccountEditionPanel(tempRepository, directory, messageLabel);
    return GridBagBuilder.init()
      .add(accountEditionPanel.getPanel(), 0, 0, 1, 1, new Insets(10, 10, 10, 10))
      .add(messageLabel, 0, 1, 1, 1, new Insets(10, 10, 10, 10))
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
