package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.accounts.AccountEditionPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.help.HyperlinkHandler;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.desktop.importer.ImportDialog;
import com.budgetview.desktop.importer.MessageHandler;
import com.budgetview.desktop.importer.components.ImportPreviewTargetCombo;
import com.budgetview.desktop.importer.edition.DateFormatSelectionPanel;
import com.budgetview.desktop.importer.edition.ImportedTransactionDateRenderer;
import com.budgetview.desktop.importer.edition.ImportedTransactionsTable;
import com.budgetview.model.Account;
import com.budgetview.model.AccountUpdateMode;
import com.budgetview.model.ImportedTransaction;
import com.budgetview.model.RealAccount;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.model.*;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.utils.Functor;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class ImportPreviewPanel extends AbstractImportStepPanel implements MessageHandler {
  private GlobRepository globalRepository;
  private LocalGlobRepository localRepository;

  private GlobRepository sessionRepository;
  private DefaultDirectory sessionDirectory;

  private JLabel fileIntroLabel = new JLabel();
  private JLabel fileNameLabel = new JLabel();
  private JLabel accountCountInfo = new JLabel();

  private Glob selectedTargetAccount;
  private ImportPreviewTargetCombo targetCombo;

  private DateFormatSelectionPanel dateFormatSelectionPanel;
  private ImportedTransactionDateRenderer dateRenderer;

  private JEditorPane errorMessage = new JEditorPane();

  private ImportedTransactionsTable importedTransactionTable;
  private AccountEditionPanel accountEditionPanel;
  private LocalGlobRepository accountEditionRepository;
  private Glob realAccount;
  private NextAction nextAction;
  private String lastExceptionDetails;
  private Glob newAccount;
  private CardHandler cardHandler;
  private JEditorPane noOperationLabel;

  private Functor importMode;

  public ImportPreviewPanel(PicsouDialog dialog,
                            ImportController controller,
                            GlobRepository repository,
                            LocalGlobRepository localRepository,
                            Directory localDirectory) {
    super(dialog, controller, localDirectory);
    this.globalRepository = repository;
    this.localRepository = localRepository;
  }

  public GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importPreviewPanel.splits", localRepository, localDirectory);
    cardHandler = builder.addCardHandler("mainCardOperations");
    noOperationLabel = new JEditorPane();
    builder.add("noOperationLabel", noOperationLabel);
    dateRenderer = new ImportedTransactionDateRenderer();
    dateFormatSelectionPanel = new DateFormatSelectionPanel(localRepository, localDirectory,
                                                            new DateFormatSelectionPanel.Callback() {
                                                              public void dateFormatSelected(String format) {
                                                                dateRenderer.changeDateFormat(format);
                                                              }
                                                            });
    builder.add("dateSelectionPanel", dateFormatSelectionPanel.getBuilder());

    sessionDirectory = new DefaultDirectory(localDirectory);
    sessionRepository = controller.getSessionRepository();

    accountEditionRepository = LocalGlobRepositoryBuilder.init(sessionRepository).get();
    accountEditionPanel = new AccountEditionPanel(dialog, accountEditionRepository, sessionDirectory);

    importedTransactionTable = new ImportedTransactionsTable(sessionRepository, sessionDirectory, dateRenderer);
    builder.add("table", importedTransactionTable.getTable());
    builder.add("fileIntroLabel", fileIntroLabel);
    builder.add("fileName", fileNameLabel);
    builder.add("accountCountInfo", accountCountInfo);

    targetCombo = new ImportPreviewTargetCombo(sessionRepository, globalRepository, new ImportPreviewTargetCombo.Callback() {
      public void processNewAccount() {
        importMode = new ImportFileMode();
        clearErrorMessage();
        selectedTargetAccount = null;
        accountEditionPanel.setAccount(newAccount);
        accountEditionPanel.setEditable(true);
      }

      public void processAccount(Glob account) {
        importMode = new ImportFileMode();
        clearErrorMessage();
        selectedTargetAccount = account;
        accountEditionPanel.clearAllMessages();
        accountEditionPanel.setAccount(selectedTargetAccount);
        accountEditionPanel.setEditable(false);
      }

      public void skipAccount() {
        importMode = new SkipFileMode();
        clearErrorMessage();
        selectedTargetAccount = null;
        accountEditionPanel.clearAccount(Lang.get("import.preview.skippedFileMessage"));
      }
    });
    builder.add("targetAccountCombo", targetCombo.get());

    registerAccountCreationListener(sessionRepository, sessionDirectory);

    final HyperlinkHandler hyperlinkHandler = new HyperlinkHandler(sessionDirectory);

    hyperlinkHandler.registerLinkAction("openErrorDetails", new Runnable() {
      public void run() {
        ImportDialog.showLastException(lastExceptionDetails, sessionDirectory);
      }
    });
    builder.add("hyperlinkHandler", hyperlinkHandler);

    builder.add("errorMessage", errorMessage);
    builder.add("accountEditionPanel", accountEditionPanel.getPanel());

    nextAction = new NextAction();
    builder.add("next", nextAction);
    builder.add("close", new CancelAction(getCancelLabel()));
    accountEditionPanel.setBalanceEditorVisible(true);
    return builder;
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

  public void prepareForDisplay() {
    accountEditionPanel.requestFocus();
  }

  public void updateForNextImport(String absolutePath, List<String> dateFormats, Glob realAccount,
                                  Integer accountNumber, Integer accountCount) {
    createPanelIfNeeded();
    this.realAccount = realAccount;

    accountEditionRepository.rollback();
    newAccount = RealAccount.createAccountFromImported(realAccount, accountEditionRepository, true);

    accountEditionPanel.setAccount(newAccount);
    localDirectory.get(SelectionService.class).select(newAccount);

    GlobList importedTransactions = sessionRepository.getAll(ImportedTransaction.TYPE);

    updateFileAndAccountMessage(absolutePath, accountNumber, accountCount);

    if (importedTransactions.isEmpty()) {
      cardHandler.show("noOperations");
      updateNoOperationMessage();
    }
    else {
      cardHandler.show("cardOperations");
    }

    if (dateFormats != null) {
      dateFormatSelectionPanel.init(importedTransactions.isEmpty() ? null : dateFormats);
    }

    Integer accountId = realAccount.get(RealAccount.ACCOUNT);
    targetCombo.preselectBestAccount(accountId, realAccount, importedTransactions);
    nextAction.setEnabled(true);
    accountEditionPanel.requestFocus();
  }

  private void updateFileAndAccountMessage(String absolutePath, Integer accountNumber, Integer accountCount) {
    boolean containsFile = Strings.isNotEmpty(absolutePath);
    fileIntroLabel.setVisible(containsFile);
    fileNameLabel.setVisible(containsFile);
    fileNameLabel.setText(absolutePath);

    if (accountCount == 1) {
      accountCountInfo.setText(Lang.get("import.preview.accountCount.one"));
    }
    else {
      accountCountInfo.setText(Lang.get("import.preview.accountCount.many", accountNumber, accountCount));
    }
  }

  private void updateNoOperationMessage() {
    String accountName = newAccount.get(Account.NAME);
    String accountNumber = newAccount.get(Account.NUMBER);
    String name = null;
    if (Strings.isNotEmpty(accountName) && Strings.isNotEmpty(accountNumber)) {
      name = accountName + " (" + accountNumber + ")";
    }
    else if (Strings.isNotEmpty(accountName)) {
      name = accountName;
    }
    else if (Strings.isNotEmpty(accountNumber)) {
      name = accountNumber;
    }
    if (Strings.isNullOrEmpty(name)) {
      noOperationLabel.setText(Lang.get("import.preview.noOperations.noName", name));
    }
    else {
      noOperationLabel.setText(Lang.get("import.preview.noOperations", name));
    }
  }

  public void showFileErrorMessage(String message) {
    createPanelIfNeeded();
    this.fileNameLabel.setText("");
    this.accountCountInfo.setText("");
    this.errorMessage.setText(message);
    nextAction.setEnabled(false);
  }

  public void showFileErrorMessage(String message, String details) {
    createPanelIfNeeded();
    showFileErrorMessage(message);
    this.lastExceptionDetails = details;
  }

  private void clearErrorMessage() {
    createPanelIfNeeded();
    this.errorMessage.setText("");
  }

  private void clearFileError() {
    lastExceptionDetails = null;
  }

  private class ImportFileMode implements Functor {
    public void run() throws Exception {
      if (selectedTargetAccount == null && !accountEditionPanel.check()) {
        Log.write("[Import] Finish account check failed");
        return;
      }
      if (selectedTargetAccount != null && Account.SUMMARY_ACCOUNT_IDS.contains(selectedTargetAccount.get(Account.ID))) {
        Log.write("[Import] Bug: using summary account");
        return;
      }
      clearErrorMessage();
      if (!dateFormatSelectionPanel.check()) {
        return;
      }
      if (selectedTargetAccount == null) {
        selectedTargetAccount = accountEditionPanel.getAccount();
        accountEditionRepository.commitChanges(false);
      }
      else {
        accountEditionRepository.rollback();
      }

      if (localRepository.contains(realAccount.getKey())) {
        localRepository.update(realAccount.getKey(), RealAccount.ENABLED, true);
      }

      sessionRepository.update(realAccount.getKey(),
                               value(RealAccount.ACCOUNT, selectedTargetAccount.get(Account.ID)));
      if (selectedTargetAccount.get(Account.LAST_IMPORT_POSITION) == null
          && Strings.isNotEmpty(realAccount.get(RealAccount.POSITION))) {
        sessionRepository.update(selectedTargetAccount.getKey(),
                                 Account.LAST_IMPORT_POSITION,
                                 Double.parseDouble(realAccount.get(RealAccount.POSITION)));
      }
      deleteOtherDuplicateAccounts(realAccount);
      Integer bankId = realAccount.get(RealAccount.BANK);
      sessionRepository.update(selectedTargetAccount.getKey(),
                               value(Account.UPDATE_MODE, AccountUpdateMode.AUTOMATIC.getId()));
      if (bankId != null && selectedTargetAccount.get(Account.BANK) == null) {
        sessionRepository.update(selectedTargetAccount.getKey(), value(Account.BANK, bankId));
      }
      newAccount = null;
      controller.completeImport(selectedTargetAccount, dateFormatSelectionPanel.getSelectedFormat());
    }

    private void deleteOtherDuplicateAccounts(Glob selectedRealAccount) {
      for (Glob realAccount : sessionRepository.getAll(RealAccount.TYPE)) {
        if (RealAccount.areStrictlyEquivalent(selectedRealAccount, realAccount) &&
            Utils.equal(selectedRealAccount.get(RealAccount.ACCOUNT), realAccount.get(RealAccount.ACCOUNT))) {
          sessionRepository.delete(realAccount);
          return;
        }
      }
    }
  }

  private class SkipFileMode implements Functor {
    public void run() throws Exception {
      newAccount = null;
      clearFileError();
      accountEditionRepository.rollback();
      controller.skipFile();
      localRepository.update(realAccount, RealAccount.ENABLED, false);
    }
  }

  private class NextAction extends AbstractAction {
    public NextAction() {
      super(Lang.get("import.preview.next"));
    }

    public void actionPerformed(ActionEvent event) {
      setEnabled(false);
      try {
        importMode.run();
      }
      catch (Exception e) {
        Log.write("Error during import", e);
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
      newAccount = null;
      clearFileError();
      controller.complete(); // missing?
      controller.closeDialog();
    }
  }

  public void dispose() {
    super.dispose();
    if (importedTransactionTable != null) {
      importedTransactionTable.dispose();
      importedTransactionTable = null;
      accountEditionPanel.dispose();
      accountEditionPanel = null;
      dateFormatSelectionPanel.dispose();
      dateFormatSelectionPanel = null;
    }
  }
}
