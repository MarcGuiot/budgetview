package org.designup.picsou.gui.importer;

import org.designup.picsou.gui.accounts.AbstractAccountPanel;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportAccountPanel {
  private JPanel panel;
  private ImportController controller;
  private LocalGlobRepository localGlobRepository;
  private AbstractAccountPanel accountPanel;
  private GlobsPanelBuilder builder;
  private Directory localDirectory;

  public ImportAccountPanel(ImportController controller, LocalGlobRepository localGlobRepository, Directory directory) {
    this.controller = controller;
    this.localDirectory = new DefaultDirectory(directory);
    this.localDirectory.add(new SelectionService());
    this.localGlobRepository =
      LocalGlobRepositoryBuilder.init(localGlobRepository)
        .copy(Bank.TYPE, BankEntity.TYPE)
        .get();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void init(PicsouDialog dialog, String closeButton) {
    builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importAccountPanel.splits", localGlobRepository, localDirectory);
    accountPanel = new AbstractAccountPanel(localGlobRepository, localDirectory);
    GlobsPanelBuilder accountBuilder =
      new GlobsPanelBuilder(getClass(), "/layout/importexport/accountPanel.splits", localGlobRepository,
                            accountPanel.getLocalDirectory());
    accountPanel.createComponents(accountBuilder, dialog);
    builder.add("accountPanel", accountBuilder);

    builder.add("import", new AbstractAction(Lang.get("load")) {
      public void actionPerformed(ActionEvent e) {
        localGlobRepository.commitChanges(false);
        controller.next();
      }
    });
    builder.add("ignore", new AbstractAction(Lang.get("ignore")) {
      public void actionPerformed(ActionEvent e) {
        localGlobRepository.rollback();
        controller.next();
      }
    });
    builder.add("close", new AbstractAction(closeButton) {

      public void actionPerformed(ActionEvent e) {
        localGlobRepository.rollback();
        controller.complete();
        controller.closeDialog();
      }
    });
    panel = builder.load();
  }

  public void setImportedAccountToImport(Glob importedAccount) {
    Glob account = RealAccount.createAccountFromImported(importedAccount, localGlobRepository, false);
    accountPanel.setAccount(account);
    this.localDirectory.get(SelectionService.class).select(account);
  }
}
