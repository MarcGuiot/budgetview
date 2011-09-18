package org.designup.picsou.gui.importer;

import org.designup.picsou.gui.accounts.AbstractAccountPanel;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.importer.edition.DateFormatSelectionPanel;
import org.designup.picsou.gui.importer.edition.ImportedTransactionDateRenderer;
import org.designup.picsou.gui.importer.edition.ImportedTransactionsTable;
import org.designup.picsou.gui.importer.utils.AccountFinder;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountUpdateMode;
import org.designup.picsou.model.ImportedTransaction;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.model.*;
import org.globsframework.model.utils.*;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;

public class ImportPreviewPanel {
  private ImportController controller;
  private GlobRepository repository;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;

  private GlobRepository sessionRepository;
  private DefaultDirectory sessionDirectory;

  private Glob currentlySelectedAccount;
  private JComboBox accountComboBox;

  private JLabel fileNameLabel = new JLabel();

  private DateFormatSelectionPanel dateFormatSelectionPanel;
  private ImportedTransactionDateRenderer dateRenderer;

  private JEditorPane message = new JEditorPane();

  private GlobsPanelBuilder builder;
  private ImportedTransactionsTable importedTransactionTable;
  private JPanel panel;
  private AbstractAccountPanel accountPanel;
  private LocalGlobRepository accountEditionRepository;
  private Glob importedAccount;

  public ImportPreviewPanel(ImportController controller,
                            Glob defaultAccount,
                            GlobRepository repository,
                            LocalGlobRepository localRepository,
                            Directory localDirectory) {
    this.controller = controller;
    this.repository = repository;
    this.localRepository = localRepository;
    this.localDirectory = localDirectory;
  }

  public void init(PicsouDialog dialog, final String textForCloseButton) {
    builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importPreviewPanel.splits", localRepository, localDirectory);
    dateRenderer = new ImportedTransactionDateRenderer();
    dateFormatSelectionPanel = new DateFormatSelectionPanel(localRepository, localDirectory,
                                                            new DateFormatSelectionPanel.Callback() {
                                                              public void dateFormatSelected(String format) {
                                                                dateRenderer.changeDateFormat(format);
                                                              }
                                                            }, message);
    builder.add("dateSelectionPanel", dateFormatSelectionPanel.getBuilder());
    sessionDirectory = new DefaultDirectory(localDirectory);
    sessionDirectory.add(new SelectionService());
    sessionDirectory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        showStep2Message("");
        currentlySelectedAccount = selection.getAll(Account.TYPE).isEmpty() ? null :
                                   selection.getAll(Account.TYPE).get(0);
        if (currentlySelectedAccount != null){
          accountPanel.clearMessage();
        }
      }
    }, Account.TYPE);

    sessionRepository = controller.getSessionRepository();

    accountEditionRepository = LocalGlobRepositoryBuilder.init(sessionRepository)
      .get();
    accountPanel = new AbstractAccountPanel(accountEditionRepository, sessionDirectory);

    importedTransactionTable = new ImportedTransactionsTable(sessionRepository, sessionDirectory, dateRenderer);
    builder.add("table", importedTransactionTable.getTable());
    builder.add("fileName", fileNameLabel);

    GlobComboView comboView = GlobComboView.init(Account.TYPE, sessionRepository, sessionDirectory)
      .setShowEmptyOption(true)
      .setEmptyOptionLabel(Lang.get("import.account.combo.empty"))
      .setFilter(new GlobMatcher() {
        public boolean matches(Glob account, GlobRepository repository) {
          return account != null &&
                 !Account.SUMMARY_ACCOUNT_IDS.contains(account.get(Account.ID));
        }
      });
    accountComboBox = comboView.getComponent();
    builder.add("accountCombo", accountComboBox);

    registerAccountCreationListener(sessionRepository, sessionDirectory);

    builder.add("importMessage", message);

    GlobsPanelBuilder accountBuilder =
      new GlobsPanelBuilder(getClass(), "/layout/importexport/accountPanel.splits", accountEditionRepository,
                            accountPanel.getLocalDirectory());
    accountPanel.createComponents(accountBuilder, dialog);
    builder.add("accountPanel", accountBuilder);

    builder.add("skipFile", new SkipFileAction());
    builder.add("finish", new FinishAction());
    builder.add("close", new CancelAction(textForCloseButton));
    this.panel = builder.load();
    accountPanel.setBalanceEditorVisible(true);
  }

  private void registerAccountCreationListener(final GlobRepository sessionRepository,
                                               final Directory sessionDirectory) {
    sessionRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        Set<Key> createdAccountKeys = changeSet.getCreated(Account.TYPE);
        if (createdAccountKeys.size() == 1) {
          Glob account = sessionRepository.get(createdAccountKeys.iterator().next());
          sessionDirectory.get(SelectionService.class).select(account);
        }
      }
    });
  }

  public void dispose() {
    builder.dispose();
    importedTransactionTable.dispose();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void showStep2Message(String message) {
    this.message.setText(message);
  }

  public void updateForNextImport(List<String> dateFormats, Glob importedAccount) {
    this.importedAccount = importedAccount;
    accountEditionRepository.rollback();
    Glob glob = RealAccount.createAccountFromImported(importedAccount, accountEditionRepository, true);

    accountPanel.setAccount(glob);
    localDirectory.get(SelectionService.class).select(glob);

    if (dateFormats != null) {
      dateFormatSelectionPanel.init(dateFormats);
    }
    Integer accountId = importedAccount.get(RealAccount.ACCOUNT);
    if (accountId != null) {
      sessionDirectory.get(SelectionService.class)
        .select(sessionRepository.get(Key.create(Account.TYPE, accountId)));
    }
    else {
      GlobList importedTransactions = sessionRepository.getAll(ImportedTransaction.TYPE);
      accountId = AccountFinder.findBestAccount(importedTransactions, repository);
      //
      Glob associatedImportedAccout = sessionRepository.getAll(RealAccount.TYPE)
        .filter(GlobMatchers.fieldEquals(RealAccount.ACCOUNT, accountId), sessionRepository)
        .getFirst();
      if (associatedImportedAccout != null && !RealAccount.areNearEquivalent(associatedImportedAccout, importedAccount)){
        accountId = null;
      }
      //
      if (accountId != null) {
        Glob account = sessionRepository.find(Key.create(Account.TYPE, accountId));
        sessionDirectory.get(SelectionService.class).select(account);
      }
      else {
        sessionDirectory.get(SelectionService.class).clear(Account.TYPE);
      }
    }
  }

  public void setFileName(String absolutePath) {
    fileNameLabel.setText(absolutePath);
  }

  private class FinishAction extends AbstractAction {
    public FinishAction() {
      super(Lang.get("import.preview.ok"));
    }

    public void actionPerformed(ActionEvent event) {
      setEnabled(false);
      try {
        if (currentlySelectedAccount == null && !accountPanel.check()) {
          return;
        }
        showStep2Message("");
        if (!dateFormatSelectionPanel.check()) {
          return;
        }
        if (currentlySelectedAccount == null) {
          currentlySelectedAccount = accountPanel.getAccount();
          accountEditionRepository.commitChanges(false);
        }
        else {
          accountEditionRepository.rollback();
        }
        
        sessionRepository.update(importedAccount.getKey(),
                                 FieldValue.value(RealAccount.ACCOUNT, currentlySelectedAccount.get(Account.ID)));
        deleteAccountIfDuplicate(importedAccount);
        sessionRepository.update(currentlySelectedAccount.getKey(), Account.UPDATE_MODE, AccountUpdateMode.AUTOMATIC.getId());
        controller.completeImport(importedAccount, currentlySelectedAccount, dateFormatSelectionPanel.getSelectedFormat());
      }
      finally {
        setEnabled(true);
      }
    }
  }

  private void deleteAccountIfDuplicate(Glob importedAccount) {
    GlobList all = sessionRepository.getAll(RealAccount.TYPE);
    for (Glob glob : all) {
      if (RealAccount.areStrictlyEquivalent(importedAccount, glob) &&
          Utils.equal(importedAccount.get(RealAccount.ACCOUNT), glob.get(RealAccount.ACCOUNT))) {
        sessionRepository.delete(glob.getKey());
        return;
      }
    }
  }

  private class SkipFileAction extends AbstractAction {
    private SkipFileAction() {
      super(Lang.get("import.skip.file"));
    }

    public void actionPerformed(ActionEvent e) {
      setEnabled(false);
      try {
        accountEditionRepository.rollback();
        controller.skipFile();
      }
      finally {
        setEnabled(true);
      }
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction(String textForCloseButton) {
      super(textForCloseButton);
    }

    public void actionPerformed(ActionEvent e) {
      controller.complete(); // missing?
      controller.closeDialog();
    }
  }
}
