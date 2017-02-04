package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.accounts.AccountEditionPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.help.HyperlinkHandler;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.desktop.importer.ImportDialog;
import com.budgetview.desktop.importer.MessageHandler;
import com.budgetview.desktop.importer.edition.DateFormatSelectionPanel;
import com.budgetview.desktop.importer.edition.ImportedTransactionDateRenderer;
import com.budgetview.desktop.importer.edition.ImportedTransactionsTable;
import com.budgetview.desktop.importer.utils.AccountFinder;
import com.budgetview.model.Account;
import com.budgetview.model.AccountUpdateMode;
import com.budgetview.model.ImportedTransaction;
import com.budgetview.model.RealAccount;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.model.*;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
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
  private GlobRepository repository;
  private LocalGlobRepository localRepository;

  private GlobRepository sessionRepository;
  private DefaultDirectory sessionDirectory;

  private JLabel fileIntroLabel = new JLabel();
  private JLabel fileNameLabel = new JLabel();
  private JLabel accountCountInfo = new JLabel();

  private Glob currentlySelectedAccount;
  private JLabel accountSelectionLabel = new JLabel();
  private JComboBox accountComboBox;

  private DateFormatSelectionPanel dateFormatSelectionPanel;
  private ImportedTransactionDateRenderer dateRenderer;

  private JEditorPane message = new JEditorPane();

  private ImportedTransactionsTable importedTransactionTable;
  private AccountEditionPanel accountEditionPanel;
  private LocalGlobRepository accountEditionRepository;
  private Glob realAccount;
  private NextAction nextAction;
  private String lastExceptionDetails;
  private Glob newAccount;
  private CardHandler cardHandler;
  private JEditorPane noOperationLabel;

  public ImportPreviewPanel(PicsouDialog dialog,
                            ImportController controller,
                            GlobRepository repository,
                            LocalGlobRepository localRepository,
                            Directory localDirectory) {
    super(dialog, controller, localDirectory);
    this.repository = repository;
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
    sessionDirectory.add(new SelectionService());
    sessionDirectory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        showStep2Message("");
        currentlySelectedAccount = selection.getAll(Account.TYPE).isEmpty() ? null :
                                   selection.getAll(Account.TYPE).get(0);
        if (currentlySelectedAccount != null) {
          accountEditionPanel.clearAllMessages();
          accountEditionPanel.setAccount(currentlySelectedAccount);
          accountEditionPanel.setEditable(false);
        }
        else if (newAccount != null) {
          accountEditionPanel.setAccount(newAccount);
          accountEditionPanel.setEditable(true);
        }
      }
    }, Account.TYPE);

    sessionRepository = controller.getSessionRepository();

    accountEditionRepository = LocalGlobRepositoryBuilder.init(sessionRepository).get();
    accountEditionPanel = new AccountEditionPanel(dialog, accountEditionRepository, sessionDirectory);

    importedTransactionTable = new ImportedTransactionsTable(sessionRepository, sessionDirectory, dateRenderer);
    builder.add("table", importedTransactionTable.getTable());
    builder.add("fileIntroLabel", fileIntroLabel);
    builder.add("fileName", fileNameLabel);
    builder.add("accountCountInfo", accountCountInfo);

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

    builder.add("accountSelectionLabel", accountSelectionLabel);

    registerAccountCreationListener(sessionRepository, sessionDirectory);

    final HyperlinkHandler hyperlinkHandler = new HyperlinkHandler(sessionDirectory);

    hyperlinkHandler.registerLinkAction("openErrorDetails", new Runnable() {
      public void run() {
        ImportDialog.showLastException(lastExceptionDetails, sessionDirectory);
      }
    });
    builder.add("hyperlinkHandler", hyperlinkHandler);

    builder.add("importMessage", message);
    builder.add("accountEditionPanel", accountEditionPanel.getPanel());

    builder.add("skipFile", new SkipFileAction());
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

  public void showStep2Message(String message) {
    createPanelIfNeeded();
    this.message.setText(message);
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

    updateFileAndAccountMessage(absolutePath, accountNumber, accountCount, !importedTransactions.isEmpty());

    if (importedTransactions.isEmpty()) {
      cardHandler.show("noOperations");
      nextAction.putValue(Action.NAME, Lang.get("import.preview.noOperation.ok"));
      updateNoOperationMessage();
    }
    else {
      cardHandler.show("cardOperations");
      nextAction.putValue(Action.NAME, Lang.get("import.preview.ok"));
    }

    if (dateFormats != null) {
      dateFormatSelectionPanel.init(importedTransactions.isEmpty() ? null : dateFormats);
    }
    Integer accountId = realAccount.get(RealAccount.ACCOUNT);
    if (accountId != null && sessionRepository.contains(Key.create(Account.TYPE, accountId))) {
      sessionDirectory.get(SelectionService.class)
        .select(sessionRepository.get(Key.create(Account.TYPE, accountId)));
    }
    else {
      accountId = AccountFinder.findBestAccount(importedTransactions, repository);

      Glob associatedImportedAccount = sessionRepository.getAll(RealAccount.TYPE)
        .filter(GlobMatchers.fieldEquals(RealAccount.ACCOUNT, accountId), sessionRepository)
        .getFirst();
      if (associatedImportedAccount != null && !RealAccount.areNearEquivalent(associatedImportedAccount, realAccount)) {
        accountId = null;
      }

      if (accountId != null && !Account.SUMMARY_ACCOUNT_IDS.contains(accountId)) {
        Glob account = sessionRepository.find(Key.create(Account.TYPE, accountId));
        sessionDirectory.get(SelectionService.class).select(account);
      }
      else {
        sessionDirectory.get(SelectionService.class).clear(Account.TYPE);
      }
    }
    nextAction.setEnabled(true);
    // necessaire sous linux au moins
    accountComboBox.repaint();
    accountEditionPanel.requestFocus();
  }

  private void updateFileAndAccountMessage(String absolutePath, Integer accountNumber, Integer accountCount, boolean hasTransactions) {

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

    if (hasTransactions) {
      accountSelectionLabel.setText(Lang.get("import.select.account"));
    }
    else {
      accountSelectionLabel.setText(Lang.get("import.select.account.empty"));
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
    this.message.setText(message);
    nextAction.setEnabled(false);
  }

  public void showFileErrorMessage(String message, String details) {
    createPanelIfNeeded();
    showFileErrorMessage(message);
    this.lastExceptionDetails = details;
  }

  private class NextAction extends AbstractAction {
    public NextAction() {
      super(Lang.get("import.preview.ok"));
    }

    public void actionPerformed(ActionEvent event) {
      setEnabled(false);
      try {
        if (currentlySelectedAccount == null && !accountEditionPanel.check()) {
          Log.write("Import: finish account check failed");
          return;
        }
        if (currentlySelectedAccount != null && Account.SUMMARY_ACCOUNT_IDS.contains(currentlySelectedAccount.get(Account.ID))) {
          Log.write("Import bug: Using summary account");
          return;
        }
        showStep2Message("");
        if (!dateFormatSelectionPanel.check()) {
          return;
        }
        if (currentlySelectedAccount == null) {
          currentlySelectedAccount = accountEditionPanel.getAccount();
          accountEditionRepository.commitChanges(false);
        }
        else {
          accountEditionRepository.rollback();
        }

        sessionRepository.update(realAccount.getKey(),
                                 value(RealAccount.ACCOUNT, currentlySelectedAccount.get(Account.ID)));
        if (currentlySelectedAccount.get(Account.LAST_IMPORT_POSITION) == null
            && Strings.isNotEmpty(realAccount.get(RealAccount.POSITION))) {
          sessionRepository.update(currentlySelectedAccount.getKey(),
                                   Account.LAST_IMPORT_POSITION,
                                   Double.parseDouble(realAccount.get(RealAccount.POSITION)));
        }
        deleteAccountIfDuplicate(realAccount);
        Integer bankId = realAccount.get(RealAccount.BANK);
        sessionRepository.update(currentlySelectedAccount.getKey(),
                                 value(Account.UPDATE_MODE, AccountUpdateMode.AUTOMATIC.getId()));
        if (bankId != null && currentlySelectedAccount.get(Account.BANK) == null){
          sessionRepository.update(currentlySelectedAccount.getKey(), value(Account.BANK, bankId));
        }
        newAccount = null;
        controller.completeImport(currentlySelectedAccount, dateFormatSelectionPanel.getSelectedFormat());
      }
      finally {
        setEnabled(true);
      }
    }
  }

  private void deleteAccountIfDuplicate(Glob selectedRealAccount) {
    for (Glob realAccount : sessionRepository.getAll(RealAccount.TYPE)) {
      if (RealAccount.areStrictlyEquivalent(selectedRealAccount, realAccount) &&
          Utils.equal(selectedRealAccount.get(RealAccount.ACCOUNT), realAccount.get(RealAccount.ACCOUNT))) {
        sessionRepository.delete(realAccount);
        return;
      }
    }
  }

  private void clearFileError() {
    lastExceptionDetails = null;
  }

  private class SkipFileAction extends AbstractAction {
    private SkipFileAction() {
      super(Lang.get("import.skip.file"));
    }

    public void actionPerformed(ActionEvent e) {
      newAccount = null;
      clearFileError();
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
      newAccount = null;
      clearFileError();
      controller.complete(); // missing?
      controller.closeDialog();
    }
  }
}
