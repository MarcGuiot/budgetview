package org.designup.picsou.gui.importer.edition;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.GlobsPanelBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class BankEntityEditionDialog {
  private GlobRepository repository;
  private Directory directory;
  private PicsouDialog dialog;

  public BankEntityEditionDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public void show(Window parent, GlobList accounts) {
    dialog = PicsouDialog.create(parent, directory);
    LocalGlobRepository localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Account.TYPE, Bank.TYPE, BankEntity.TYPE)
      .get();
    BankEntityEditionPanel panel = new BankEntityEditionPanel(dialog, localRepository, directory);
    panel.init(accounts);
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/bankEntityEditionDialog.splits",
                                                      localRepository, directory);

    builder.add("editionPanel", panel.getPanel());
    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new ValidateAction(localRepository),
                               new CancelAction());
    dialog.pack();
    dialog.showCentered(true);
    panel.dispose();
    builder.dispose();
  }

  private class ValidateAction extends AbstractAction {
    private LocalGlobRepository repository;

    public ValidateAction(LocalGlobRepository repository) {
      super(Lang.get("ok"));
      this.repository = repository;
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
      repository.commitChanges(true);
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
      System.out.println("BankEntityEditionDialog$CancelAction.actionPerformed");
    }
  }
}
