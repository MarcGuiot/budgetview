package org.designup.picsou.gui.importer;

import org.designup.picsou.gui.accounts.AccountEditionPanel;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportAccountPanel {
  private JPanel panel;
  private ImportController controller;
  private LocalGlobRepository localGlobRepository;
  private AccountEditionPanel accountPanel;
  private GlobsPanelBuilder builder;
  private Directory localDirectory;
  private Glob importedAccount;

  public ImportAccountPanel(ImportController controller, LocalGlobRepository localGlobRepository, Directory directory) {
    this.controller = controller;
    this.localDirectory = new DefaultDirectory(directory);
    this.localDirectory.add(new SelectionService());
    this.localGlobRepository =
      LocalGlobRepositoryBuilder.init(localGlobRepository)
        .copy(Bank.TYPE, BankEntity.TYPE, RealAccount.TYPE)
        .get();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void init(PicsouDialog dialog, String closeButton) {
    builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importAccountPanel.splits", localGlobRepository, localDirectory);

    accountPanel = new AccountEditionPanel(dialog, localGlobRepository, localDirectory);
    builder.add("accountPanel", accountPanel.getPanel());

    builder.add("import", new AbstractAction(Lang.get("load")) {
      public void actionPerformed(ActionEvent e) {
        if (accountPanel.check()) {
          localGlobRepository.update(importedAccount.getKey(),
                                     FieldValue.value(RealAccount.ACCOUNT, accountPanel.getAccount().get(Account.ID)));

          localGlobRepository.commitChanges(false);
          controller.next();
        }
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
    localGlobRepository.rollback();
    this.importedAccount = importedAccount;
    Glob account = RealAccount.createAccountFromImported(importedAccount, localGlobRepository, false);
    accountPanel.setAccount(account);
    this.localDirectory.get(SelectionService.class).select(account);
  }

  public void dispose() {
    builder.dispose();
    accountPanel.dispose();
  }
}
