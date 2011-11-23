package org.designup.picsou.gui.importer;

import org.designup.picsou.gui.accounts.AccountEditionPanel;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.help.HyperlinkHandler;
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
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.model.*;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.*;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;

public class ImportPreviewPanel implements MessageHandler {
  private ImportController controller;
  private GlobRepository repository;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;

  private GlobRepository sessionRepository;
  private DefaultDirectory sessionDirectory;

  private JLabel fileNameLabel = new JLabel();
  private JEditorPane accountCountInfo = new JEditorPane();

  private Glob currentlySelectedAccount;
  private JLabel accountSelectionLabel = new JLabel();
  private JComboBox accountComboBox;

  private DateFormatSelectionPanel dateFormatSelectionPanel;
  private ImportedTransactionDateRenderer dateRenderer;

  private JEditorPane message = new JEditorPane();

  private GlobsPanelBuilder builder;
  private ImportedTransactionsTable importedTransactionTable;
  private JPanel panel;
  private AccountEditionPanel accountEditionPanel;
  private LocalGlobRepository accountEditionRepository;
  private Glob importedAccount;
  private ImportPreviewPanel.FinishAction finishAction;
  private Exception lastException;
  private Glob newAccount;
  private CardHandler cardHandler;
  private JEditorPane noOperationLabel;

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
    cardHandler = builder.addCardHandler("mainCardOperations");
    noOperationLabel = new JEditorPane();
    builder.add("noOperationLabel", noOperationLabel);
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

    final HyperlinkHandler hyperlinkHandler = new HyperlinkHandler(sessionDirectory, dialog);

    hyperlinkHandler.registerLinkAction("openErrorDetails", new Runnable() {
      public void run() {
        ImportDialog.showLastException(lastException, sessionDirectory);
      }
    });
    builder.add("hyperlinkHandler", hyperlinkHandler);

    builder.add("importMessage", message);
    builder.add("accountEditionPanel", accountEditionPanel.getPanel());

    builder.add("skipFile", new SkipFileAction());
    finishAction = new FinishAction();
    builder.add("finish", finishAction);
    builder.add("close", new CancelAction(textForCloseButton));
    this.panel = builder.load();
    accountEditionPanel.setBalanceEditorVisible(true);
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

  public void updateForNextImport(List<String> dateFormats, Glob importedAccount,
                                  Integer accountNumber, Integer accountCount) {
    this.importedAccount = importedAccount;
    accountEditionRepository.rollback();
    newAccount = RealAccount.createAccountFromImported(importedAccount, accountEditionRepository, true);

    accountEditionPanel.setAccount(newAccount);
    localDirectory.get(SelectionService.class).select(newAccount);

    GlobList importedTransactions = sessionRepository.getAll(ImportedTransaction.TYPE);

    updateFileAndAccountMessage(accountNumber, accountCount, !importedTransactions.isEmpty());

    if (importedTransactions.isEmpty()) {
      cardHandler.show("noOperations");
      finishAction.putValue(Action.NAME, Lang.get("import.preview.noOperation.ok"));
      updateNoOperationMessage();
    }
    else {
      cardHandler.show("cardOperations");
      finishAction.putValue(Action.NAME, Lang.get("import.preview.ok"));
    }

    if (dateFormats != null) {
      dateFormatSelectionPanel.init(importedTransactions.isEmpty() ? null : dateFormats);
    }
    Integer accountId = importedAccount.get(RealAccount.ACCOUNT);
    if (accountId != null && sessionRepository.contains(Key.create(Account.TYPE, accountId))) {
      sessionDirectory.get(SelectionService.class)
        .select(sessionRepository.get(Key.create(Account.TYPE, accountId)));
    }
    else {
      accountId = AccountFinder.findBestAccount(importedTransactions, repository);

      Glob associatedImportedAccout = sessionRepository.getAll(RealAccount.TYPE)
        .filter(GlobMatchers.fieldEquals(RealAccount.ACCOUNT, accountId), sessionRepository)
        .getFirst();
      if (associatedImportedAccout != null && !RealAccount.areNearEquivalent(associatedImportedAccout, importedAccount)) {
        accountId = null;
      }

      if (accountId != null) {
        Glob account = sessionRepository.find(Key.create(Account.TYPE, accountId));
        sessionDirectory.get(SelectionService.class).select(account);
      }
      else {
        sessionDirectory.get(SelectionService.class).clear(Account.TYPE);
      }
    }
  }

  private void updateFileAndAccountMessage(Integer accountNumber, Integer accountCount, boolean hasTransactions) {
    String transactionsSuffix = hasTransactions ? ".with.operations" : ".without.operations";
    if (accountCount == 1) {
      accountCountInfo.setText(Lang.get("import.preview.accountMessage.one" + transactionsSuffix));
    }
    else {
      accountCountInfo.setText(Lang.get("import.preview.accountMessage.many" + transactionsSuffix,
                                        accountNumber, accountCount));
    }

    if (hasTransactions) {
      accountSelectionLabel.setText(Lang.get("import.select.account"));
    }
    else {
      accountSelectionLabel.setText(Lang.get("import.select.account.empty"));
    }
  }

  private void updateNoOperationMessage() {
    String accName = newAccount.get(Account.NAME);
    String accNumber = newAccount.get(Account.NUMBER);
    String name = null;
    if (Strings.isNotEmpty(accName) && Strings.isNotEmpty(accNumber)) {
      name = accName + " (" + accNumber + ")";
    }
    else if (Strings.isNotEmpty(accName)) {
      name = accName;
    }
    else if (Strings.isNotEmpty(accNumber)) {
      name = accNumber;
    }
    if (Strings.isNullOrEmpty(name)) {
      noOperationLabel.setText(Lang.get("import.preview.noOperations.noName", name));
    }
    else {
      noOperationLabel.setText(Lang.get("import.preview.noOperations", name));
    }
  }

  public void setFileName(String absolutePath) {
    fileNameLabel.setText(absolutePath);
  }

  public void showFileErrorMessage(String message) {
    this.setFileName("");
    this.accountCountInfo.setText("");
    this.message.setText(message);
    finishAction.setEnabled(false);
  }

  public void showFileErrorMessage(String message, Exception exception) {
    showFileErrorMessage(message);
    this.message.setText(message);
  }

  private class FinishAction extends AbstractAction {
    public FinishAction() {
      super(Lang.get("import.preview.ok"));
    }

    public void actionPerformed(ActionEvent event) {
      Log.write("finish");
      setEnabled(false);
      try {
        if (currentlySelectedAccount == null && !accountEditionPanel.check()) {
          Log.write("finish account check fail");
          return;
        }
        showStep2Message("");
        if (!dateFormatSelectionPanel.check()) {
          Log.write("finish missing date");
          return;
        }
        if (currentlySelectedAccount == null) {
          currentlySelectedAccount = accountEditionPanel.getAccount();
          accountEditionRepository.commitChanges(false);
        }
        else {
          accountEditionRepository.rollback();
        }

        sessionRepository.update(importedAccount.getKey(),
                                 FieldValue.value(RealAccount.ACCOUNT, currentlySelectedAccount.get(Account.ID)));
        deleteAccountIfDuplicate(importedAccount);
        sessionRepository.update(currentlySelectedAccount.getKey(), Account.UPDATE_MODE, AccountUpdateMode.AUTOMATIC.getId());
        newAccount = null;
        controller.completeImport(importedAccount, currentlySelectedAccount, dateFormatSelectionPanel.getSelectedFormat());
        Log.write("finish ok");
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

  private void clearFileError() {
    lastException = null;
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
