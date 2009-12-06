package org.designup.picsou.gui.importer;

import org.designup.picsou.gui.accounts.AccountPositionEditionDialog;
import org.designup.picsou.gui.accounts.Day;
import org.designup.picsou.gui.accounts.NewAccountAction;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.importer.additionalactions.AccountEditionAction;
import org.designup.picsou.gui.importer.additionalactions.BankEntityEditionAction;
import org.designup.picsou.gui.importer.additionalactions.CardTypeAction;
import org.designup.picsou.gui.importer.edition.BrowseFilesAction;
import org.designup.picsou.gui.importer.edition.DateFormatSelectionPanel;
import org.designup.picsou.gui.importer.edition.ImportedTransactionDateRenderer;
import org.designup.picsou.gui.importer.edition.ImportedTransactionsTable;
import org.designup.picsou.importer.BankFileType;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.SingleComponentLayout;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.AbstractDocumentListener;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import com.jidesoft.swing.AutoResizingTextArea;

public class ImportDialog {

  private GlobRepository repository;
  private Directory directory;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;

  private ImportController controller;

  private GlobRepository sessionRepository;
  private DefaultDirectory sessionDirectory;

  private boolean usePreferredPath;

  private JLabel messageLabel = new JLabel();
  private JPanel filePanel = new JPanel();
  private final JTextField fileField = new JTextField();
  private JButton fileButton = new JButton();
  private JLabel fileNameLabel = new JLabel();

  private Key currentlySelectedAccount;

  private DateFormatSelectionPanel dateFormatSelectionPanel;

  private JButton newAccountButton;
  private JComboBox accountComboBox;
  private JLabel importMessageLabel = new JLabel();
  private ImportedTransactionDateRenderer dateRenderer;
  private Glob defaultAccount;

  private JPanel mainPanel;
  private JPanel step1Panel;
  private JPanel step2Panel;
  private PicsouDialog dialog;

  private List<AdditionalImportAction> additionalImportActions = new ArrayList<AdditionalImportAction>();
  private List<AdditionalImportAction> currentActions;
  private Repeat<AdditionalImportAction> additionalActionImportRepeat;

  public ImportDialog(String textForCloseButton, List<File> files, Glob defaultAccount,
                      final Window owner, final GlobRepository repository, Directory directory,
                      boolean usePreferredPath) {

    this.defaultAccount = defaultAccount;
    this.repository = repository;
    this.directory = directory;
    this.usePreferredPath = usePreferredPath;

    updateFileField(files);

    loadLocalRepository(repository);

    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());

    controller = new ImportController(this, fileField, repository, localRepository, directory);

    dialog = PicsouDialog.create(owner, directory);
    dialog.setOpenRequestIsManaged(true);

    initStep1Panel(textForCloseButton, directory);
    initStep2Panel(textForCloseButton, owner);
    initMainPanel();

    dialog.setContentPane(mainPanel);

    if (defaultAccount != null) {
      Glob bank = Account.getBank(defaultAccount, localRepository);
      localDirectory.get(SelectionService.class).select(bank);
    }
  }

  private void initMainPanel() {
    mainPanel = new JPanel();
    mainPanel.setLayout(new SingleComponentLayout(null));
    mainPanel.add(step1Panel);
  }

  private void initStep1Panel(String textForCloseButton, Directory directory) {
    fileButton.setAction(new BrowseFilesAction(fileField, localRepository, usePreferredPath, dialog));

    initFileField();

    GlobsPanelBuilder builder1 = new GlobsPanelBuilder(getClass(), "/layout/importDialogStep1.splits", localRepository, localDirectory);
    builder1.add("importMessage", messageLabel);
    builder1.add("filePanel", filePanel);
    builder1.add("fileField", fileField);
    builder1.add("fileButton", fileButton);
    builder1.add("import", new ImportAction());
    builder1.add("close", new AbstractAction(textForCloseButton) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        closeDialog();
      }
    });
    builder1.add("hyperlinkHandler", new HyperlinkHandler(directory, dialog));

    step1Panel = builder1.load();
  }

  private void initFileField() {
    fileField.getDocument().addDocumentListener(new AbstractDocumentListener() {
      protected void documentChanged(DocumentEvent e) {
        clearErrorMessage();
      }
    });
  }

  private void initStep2Panel(final String textForCloseButton, Window owner) {
    GlobsPanelBuilder builder2 = new GlobsPanelBuilder(getClass(), "/layout/importDialogStep2.splits",
                                                       localRepository, localDirectory);
    dateRenderer = new ImportedTransactionDateRenderer();
    dateFormatSelectionPanel = new DateFormatSelectionPanel(localRepository, localDirectory,
                                                            new DateFormatSelectionPanel.Callback() {
                                                              public void dateFormatSelected(String format) {
                                                                dateRenderer.changeDateFormat(format);
                                                              }
                                                            }, importMessageLabel);
    builder2.add("dateSelectionPanel", dateFormatSelectionPanel.getBuilder());
    sessionDirectory = new DefaultDirectory(localDirectory);
    sessionDirectory.add(new SelectionService());
    sessionDirectory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentlySelectedAccount = selection.getAll(Account.TYPE).isEmpty() ? null :
                                   selection.getAll(Account.TYPE).get(0).getKey();
      }
    }, Account.TYPE);

    sessionRepository = controller.getSessionRepository();

    ImportedTransactionsTable table = new ImportedTransactionsTable(sessionRepository, sessionDirectory, dateRenderer);
    builder2.add("table", table.getTable());
    builder2.add("fileName", fileNameLabel);

    NewAccountAction newAccountAction =
      new NewAccountAction(AccountType.MAIN, sessionRepository, sessionDirectory, dialog)
        .setUpdateModeEditable(false);
    newAccountButton = builder2.add("newAccount", new JButton(newAccountAction)).getComponent();

    GlobComboView comboView = GlobComboView.init(Account.TYPE, sessionRepository, sessionDirectory);
    accountComboBox = comboView.getComponent();
    builder2.add("accountCombo", accountComboBox);
    comboView.setFilter(new GlobMatcher() {
      public boolean matches(Glob account, GlobRepository repository) {
        return account != null &&
               !Account.SUMMARY_ACCOUNT_IDS.contains(account.get(Account.ID)) &&
               AccountUpdateMode.AUTOMATIC.getId().equals(account.get(Account.UPDATE_MODE));
      }
    });

    registerAccountCreationListener(sessionRepository, sessionDirectory);

    loadAdditionalImportActions();

    builder2.add("importMessage", importMessageLabel);

    additionalActionImportRepeat =
      builder2.addRepeat("additionalActions", Collections.<AdditionalImportAction>emptyList(),
                         new RepeatComponentFactory<AdditionalImportAction>() {
                           public void registerComponents(RepeatCellBuilder cellBuilder,
                                                          final AdditionalImportAction item) {
                             cellBuilder.add("message", new AutoResizingTextArea(item.getMessage()));
                             cellBuilder.add("action", new AbstractAction(item.getButtonMessage()) {
                               public void actionPerformed(ActionEvent e) {
                                 item.getAction().actionPerformed(e);
                                 updateAdditionalImportActions();
                               }
                             });
                           }
                         });

    builder2.add("skipFile", new SkipFileAction());
    builder2.add("finish", new FinishAction());
    builder2.add("close", new CancelAction(textForCloseButton));
    this.step2Panel = builder2.load();
  }

  private void loadAdditionalImportActions() {
    additionalImportActions.addAll(Arrays.asList(
      new BankEntityEditionAction(dialog, sessionRepository, sessionDirectory),
      new AccountEditionAction(dialog, sessionRepository, sessionDirectory),
      new CardTypeAction(dialog, sessionRepository, sessionDirectory)));
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

  protected void contentChanged() {
    dialog.pack();
    GuiUtils.center(dialog);
  }

  private void loadLocalRepository(GlobRepository repository) {
    GlobType[] globTypes = {Bank.TYPE, BankEntity.TYPE, Day.TYPE,
                            Account.TYPE, AccountUpdateMode.TYPE,
                            Transaction.TYPE, Month.TYPE, UserPreferences.TYPE};

    if (localRepository == null) {
      this.localRepository = LocalGlobRepositoryBuilder.init(repository)
        .copy(globTypes).get();
    }
    else {
      this.localRepository.rollback();
    }
  }

  public void updateFileField(List<File> files) {
    StringBuilder builder = new StringBuilder(fileField.getText());
    for (File file : files) {
      if (builder.length() != 0) {
        builder.append(";");
      }
      builder.append(file.getAbsolutePath());
    }
    fileField.setText(builder.toString());
  }

  protected void closeDialog() {
    dialog.setVisible(false);
  }

  private boolean initialFileAccepted() {
    String path = fileField.getText();
    if (Strings.isNullOrEmpty(path)) {
      displayErrorMessage("login.data.file.required");
      return false;
    }

    String[] strings = path.split(";");
    for (String fileName : strings) {
      File file = new File(fileName);
      if (BankFileType.getTypeFromName(fileName) == null) {
        displayErrorMessage("import.invalid.extension");
        return false;
      }
      if (!file.exists()) {
        displayErrorMessage("login.data.file.not.found");
        return false;
      }
    }
    return true;
  }

  private void displayErrorMessage(String key) {
    showMessage("<html><font color=red>" + Lang.get(key) + "</font></html>");
  }

  private void clearErrorMessage() {
    showMessage("");
  }

  public void show() {
    final PicsouFrame frame = (PicsouFrame)directory.get(JFrame.class);
    if (frame.isIconified()) {
      frame.addWindowListener(new WindowAdapter() {
        public void windowDeiconified(WindowEvent e) {
          frame.removeWindowListener(this);
          dialog.pack();
          dialog.showCentered();
        }
      });
      final JDialog dialog = new JDialog(frame);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          while (!dialog.isVisible()) {
            SwingUtilities.invokeLater(this);
          }
          dialog.setVisible(false);
        }
      });
      dialog.setModal(true);
      dialog.setVisible(true);
    }
    else {
      dialog.pack();
      dialog.showCentered();
    }
  }

  public void setFileName(String absolutePath) {
    fileNameLabel.setText(absolutePath);
  }

  public void showLastImportedMonthAndClose(Set<Integer> months) {
    GlobList monthsToSelect =
      repository.getAll(Month.TYPE, GlobMatchers.fieldIn(Month.ID, months)).sort(Month.ID);
    if (!monthsToSelect.isEmpty()) {
      SelectionService selectionService = directory.get(SelectionService.class);
      selectionService.select(monthsToSelect.getLast());
    }
    closeDialog();
  }

  public void showMessage(String message) {
    messageLabel.setText(message);
  }

  public void updateForNextImport(File file, List<String> dateFormats) throws IOException {
    updateAdditionalImportActions();
    initQifAccountChooserFields(file);
    dateFormatSelectionPanel.init(dateFormats);
  }

  public void showStep2() {
    mainPanel.removeAll();
    mainPanel.add(step2Panel);
    contentChanged();
  }

  private void updateAdditionalImportActions() {
    currentActions = new ArrayList<AdditionalImportAction>();
    for (AdditionalImportAction action : additionalImportActions) {
      if (!action.isValid()) {
        currentActions.add(action);
      }
    }
    additionalActionImportRepeat.set(currentActions);
  }

  public void showPositionDialog() {
    Set<Key> transactions = localRepository.getCurrentChanges().getCreated(Transaction.TYPE);
    Set<Integer> accounts = new HashSet<Integer>();
    for (Key transaction : transactions) {
      accounts.add(localRepository.get(transaction).get(Transaction.ACCOUNT));
    }
    for (Integer accountId : accounts) {
      Glob account = localRepository.get(Key.create(Account.TYPE, accountId));
      if (account.get(Account.POSITION) == null) {
        AccountPositionEditionDialog dialog =
          new AccountPositionEditionDialog(account, true, localRepository, localDirectory, this.dialog);
        dialog.show();
      }
    }
  }

  private void initQifAccountChooserFields(File file) {
    if (BankFileType.getTypeFromName(file.getAbsolutePath()).equals(BankFileType.QIF)) {
      GlobList accounts = sessionRepository.getAll(Account.TYPE);
      for (Integer accountId : Account.SUMMARY_ACCOUNT_IDS) {
        accounts.remove(sessionRepository.get(Key.create(Account.TYPE, accountId)));
      }
      if (accounts.size() != 0) {
        Glob account = null;
        if (defaultAccount != null) {
          account = sessionRepository.get(defaultAccount.getKey());
        }
        else if (accounts.size() == 1) {
          account = accounts.get(0);
        }
        if (account != null) {
          sessionDirectory.get(SelectionService.class).select(account);
        }
      }
      accountComboBox.setVisible(!accounts.isEmpty());
      newAccountButton.setVisible(!accounts.isEmpty());
    }
    else {
      accountComboBox.setVisible(false);
      newAccountButton.setVisible(false);
    }
  }

  private class ImportAction extends AbstractAction {
    public ImportAction() {
      super(Lang.get("import.ok"));
    }

    public void actionPerformed(ActionEvent event) {
      if (!initialFileAccepted()) {
        return;
      }
      controller.doImport();
    }
  }

  private class FinishAction extends AbstractAction {
    public FinishAction() {
      super(Lang.get("import.ok"));
    }

    public void actionPerformed(ActionEvent event) {
      setEnabled(false);
      try {
        showMessage("");
        if (!dateFormatSelectionPanel.check()) {
          return;
        }
        if (!currentActions.isEmpty()) {
          return;
        }
        controller.finish(currentlySelectedAccount, dateFormatSelectionPanel.getSelectedFormat());
      }
      finally {
        setEnabled(true);
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
      closeDialog();
    }
  }
}