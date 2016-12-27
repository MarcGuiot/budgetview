package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.accounts.AccountEditionPanel;
import com.budgetview.desktop.accounts.utils.MonthDay;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.model.Account;
import com.budgetview.model.Bank;
import com.budgetview.model.BankEntity;
import com.budgetview.model.RealAccount;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportAccountPanel extends AbstractImportStepPanel {
  private LocalGlobRepository localGlobRepository;
  private AccountEditionPanel accountPanel;
  private Glob importedAccount;

  public ImportAccountPanel(PicsouDialog dialog, ImportController controller, LocalGlobRepository localGlobRepository, Directory directory) {
    super(dialog, controller, new DefaultDirectory(directory));
    this.localDirectory.add(new SelectionService());
    this.localGlobRepository =
      LocalGlobRepositoryBuilder.init(localGlobRepository)
        .copy(Bank.TYPE, BankEntity.TYPE, MonthDay.TYPE, RealAccount.TYPE)
        .get();
  }

  public GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importAccountPanel.splits", localGlobRepository, localDirectory);

    accountPanel = new AccountEditionPanel(dialog, localGlobRepository, localDirectory);
    builder.add("accountPanel", accountPanel.getPanel());

    builder.add("import", new AbstractAction(Lang.get("load")) {
      public void actionPerformed(ActionEvent e) {
        if (accountPanel.check()) {
          localGlobRepository.update(importedAccount.getKey(),
                                     RealAccount.ACCOUNT,
                                     accountPanel.getAccount().get(Account.ID));

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
    builder.add("close", new AbstractAction(getCancelLabel()) {
      public void actionPerformed(ActionEvent e) {
        localGlobRepository.rollback();
        controller.complete();
        controller.closeDialog();
      }
    });
    return builder;
  }

  public void prepareForDisplay() {
    getPanel(); // force creation
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
    super.dispose();
    if (accountPanel != null) {
      accountPanel.dispose();
      accountPanel = null;
    }
  }
}
