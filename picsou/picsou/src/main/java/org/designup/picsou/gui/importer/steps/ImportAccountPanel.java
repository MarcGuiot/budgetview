package org.designup.picsou.gui.importer.steps;

import org.designup.picsou.gui.accounts.AccountEditionPanel;
import org.designup.picsou.gui.accounts.utils.MonthDay;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.importer.ImportController;
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

public class ImportAccountPanel extends AbstractImportStepPanel {
  private JPanel panel;
  private LocalGlobRepository localGlobRepository;
  private AccountEditionPanel accountPanel;
  private GlobsPanelBuilder builder;
  private Glob importedAccount;

  public ImportAccountPanel(PicsouDialog dialog,
                            String textForCloseButton,
                            ImportController controller, LocalGlobRepository localGlobRepository, Directory directory) {
    super(dialog, textForCloseButton, controller, new DefaultDirectory(directory));
    this.localDirectory.add(new SelectionService());
    this.localGlobRepository =
      LocalGlobRepositoryBuilder.init(localGlobRepository)
        .copy(Bank.TYPE, BankEntity.TYPE, MonthDay.TYPE, RealAccount.TYPE)
        .get();
  }

  public void createPanelIfNeeded() {
    if (builder != null) {
      return;
    }

    builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importAccountPanel.splits", localGlobRepository, localDirectory);

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
    builder.add("close", new AbstractAction(textForCloseButton) {

      public void actionPerformed(ActionEvent e) {
        localGlobRepository.rollback();
        controller.complete();
        controller.closeDialog();
      }
    });
    panel = builder.load();
  }

  public JPanel getPanel() {
    createPanelIfNeeded();
    return panel;
  }

  public void requestFocus() {
    createPanelIfNeeded();
    accountPanel.requestFocus();
  }

  public void setImportedAccountToImport(Glob importedAccount) {
    createPanelIfNeeded();
    localGlobRepository.rollback();
    this.importedAccount = importedAccount;
    Glob account = RealAccount.createAccountFromImported(importedAccount, localGlobRepository, false);
    accountPanel.setAccount(account);
    this.localDirectory.get(SelectionService.class).select(account);
  }

  public void dispose() {
    if (builder != null) {
      builder.dispose();
      accountPanel.dispose();
      builder = null;
    }
  }
}
