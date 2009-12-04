package org.designup.picsou.gui.importer;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.accounts.AccountPositionEditionDialog;
import org.designup.picsou.gui.accounts.Day;
import org.designup.picsou.gui.accounts.NewAccountAction;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.startup.AutoCategorizationFunctor;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.importer.BankFileType;
import org.designup.picsou.importer.ImportSession;
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
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.gui.views.utils.LabelCustomizers;
import static org.globsframework.gui.views.utils.LabelCustomizers.chain;
import static org.globsframework.gui.views.utils.LabelCustomizers.fontSize;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.utils.*;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ImportPanel {
  private JLabel messageLabel = new JLabel();
  private JPanel filePanel = new JPanel();
  private final JTextField fileField = new JTextField();
  private JButton fileButton = new JButton();
  private JLabel fileNameLabel = new JLabel();

  private GlobRepository repository;
  private Directory directory;
  private boolean usePreferedPath;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;

  private DefaultDirectory sessionDirectory;
  private ImportSession importSession;
  private final List<File> files = new ArrayList<File>();
  private Key currentlySelectedAccount;

  private DateFormatSelectionPanel dateFormatSelectionPanel;

  private JButton newAccountButton;
  private JComboBox accountComboBox;
  private JLabel importMessageLabel = new JLabel();
  private GlobRepository sessionRepository;
  private ImportedTransactionDateRenderer dateRenderer;
  private boolean step1 = true;
  private boolean step2 = true;
  private boolean completed = false;
  private OpenRequestManager openRequestManager;
  private Glob defaultAccount;
  private Set<Integer> importKeys = new HashSet<Integer>();
  private JPanel mainPanel;
  private JPanel panelStep1;
  private JPanel panelStep2;
  private List<AdditionalImportAction> actions = new ArrayList<AdditionalImportAction>();

  private static final int[] COLUMN_SIZES = {10, 45};

  private PicsouDialog dialog;
  private Repeat<AdditionalImportAction> additionalActionImportRepeat;
  private List<AdditionalImportAction> currentActions;

  public ImportPanel(String textForCloseButton, List<File> files, Glob defaultAccount,
                     final Window owner, final GlobRepository repository, Directory directory,
                     boolean usePreferedPath) {

    this.defaultAccount = defaultAccount;
    this.repository = repository;
    this.directory = directory;
    this.usePreferedPath = usePreferedPath;

    updateFileField(files);
    initOpenRequestManager(directory);
    loadLocalRepository(repository);

    this.localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());

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
    mainPanel.add(panelStep1);
  }

  private void initStep1Panel(String textForCloseButton, Directory directory) {
    fileButton.setAction(new BrowseFilesAction());

    initFileField();

    GlobsPanelBuilder builder1 = new GlobsPanelBuilder(getClass(), "/layout/importPanelStep1.splits", localRepository, localDirectory);
    builder1.add("importMessage", messageLabel);
    builder1.add("filePanel", filePanel);
    builder1.add("fileField", fileField);
    builder1.add("fileButton", fileButton);
    builder1.add("import", new ImportAction());
    builder1.add("close", new AbstractAction(textForCloseButton) {
      public void actionPerformed(ActionEvent e) {
        openRequestManager.popCallback();
        complete();
      }
    });
    builder1.add("hyperlinkHandler", new HyperlinkHandler(directory, dialog));

    panelStep1 = builder1.load();
  }

  private void initFileField() {
    fileField.getDocument().addDocumentListener(new AbstractDocumentListener() {
      protected void documentChanged(DocumentEvent e) {
        clearErrorMessage();
      }
    });
  }

  private void initStep2Panel(final String textForCloseButton, Window owner) {
    GlobsPanelBuilder builder2 = new GlobsPanelBuilder(getClass(), "/layout/importPanelStep2.splits",
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

    importSession = new ImportSession(localRepository, sessionDirectory);
    sessionRepository = importSession.getTempRepository();

    GlobTableView tableView = GlobTableView.init(ImportedTransaction.TYPE, sessionRepository,
                                                 dateRenderer.getComparator(), sessionDirectory)
      .addColumn(Lang.get("import.bankDate"), ImportedTransaction.BANK_DATE,
                 chain(fontSize(9), dateRenderer))
      .addColumn(Lang.get("label"), new TransactionLabelGlobStringifier(), LabelCustomizers.autoTooltip())
      .addColumn(Lang.get("amount"), ImportedTransaction.AMOUNT);

    PicsouTableHeaderPainter.install(tableView, localDirectory);

    JTable transactionTable = tableView.getComponent();
    dateRenderer.setTable(tableView);
    ApplicationColors.setSelectionColors(transactionTable, directory);
    Gui.setColumnSizes(transactionTable, COLUMN_SIZES);

    builder2.add("table", transactionTable);
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

    AdditionalImportAction bankEntity = new BankEntityEditionAction(dialog, sessionRepository, sessionDirectory);
    actions.add(bankEntity);

    AdditionalImportAction accountEdition = new AccountEditionAction(dialog, sessionRepository, sessionDirectory);
    actions.add(accountEdition);

    AdditionalImportAction cardType = new CardTypeAction(dialog, sessionRepository, sessionDirectory);
    actions.add(cardType);

    builder2.add("importMessage", importMessageLabel);

    additionalActionImportRepeat = builder2.addRepeat("additionalActions", Collections.<AdditionalImportAction>emptyList(),
                                                      new RepeatComponentFactory<AdditionalImportAction>() {
                                                        public void registerComponents(RepeatCellBuilder cellBuilder, final AdditionalImportAction item) {
                                                          cellBuilder.add("message", new JLabel(item.getMessage()));
                                                          cellBuilder.add("action", new AbstractAction(item.getButtonMessage()) {
                                                            public void actionPerformed(ActionEvent e) {
                                                              item.getAction().actionPerformed(e);
                                                              updateActions();
                                                            }
                                                          });
                                                        }
                                                      });

    builder2.add("skipFile", new SkipFileAction());
    builder2.add("finish", new FinishAction());
    builder2.add("close", new CancelAction(textForCloseButton));
    this.panelStep2 = builder2.load();
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

  private void initOpenRequestManager(Directory directory) {
    openRequestManager = directory.get(OpenRequestManager.class);
    openRequestManager.pushCallback(new OpenRequestManager.Callback() {
      public boolean accept() {
        synchronized (fileField) {
          if (step1) {
            return true;
          }
        }
        return false;
      }

      public void openFiles(final List<File> files) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            synchronized (fileField) {
              if (step1) {
                updateFileField(files);
              }
              else {
                openRequestManager.openFiles(files);
              }
            }
          }
        });
      }
    });
  }

  private void showStep(JPanel step) {
    mainPanel.removeAll();
    mainPanel.add(step);
    contentChanged();
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

  private void updateFileField(List<File> files) {
    StringBuilder builder = new StringBuilder(fileField.getText());
    for (File file : files) {
      if (builder.length() != 0) {
        builder.append(";");
      }
      builder.append(file.getAbsolutePath());
    }
    fileField.setText(builder.toString());
  }

  protected void complete() {
    dialog.setVisible(false);
  }

  private File[] getInitialFiles() {
    synchronized (fileField) {
      String path = fileField.getText();
      String[] strings = path.split(";");
      File[] files = new File[strings.length];
      for (int i = 0; i < strings.length; i++) {
        String string = strings[i];
        files[i] = new File(string);
      }
      if (Strings.isNullOrEmpty(path)) {
        return null;
      }
      return files;
    }
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
    messageLabel.setText("<html><font color=red>" + Lang.get(key) + "</font></html>");
  }

  private void clearErrorMessage() {
    messageLabel.setText("");
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

  private class ImportAction extends AbstractAction {
    public ImportAction() {
      super(Lang.get("import.ok"));
    }

    public void actionPerformed(ActionEvent event) {
      if (!initialFileAccepted()) {
        return;
      }
      openRequestManager.popCallback();
      openRequestManager.pushCallback(new OpenRequestManager.Callback() {
        public boolean accept() {
          synchronized (ImportPanel.this.files) {
            if (step2) {
              return true;
            }
          }
          return false;
        }

        public void openFiles(final List<File> files) {
          synchronized (ImportPanel.this.files) {
            if (step2) {
              ImportPanel.this.files.addAll(files);
            }
            else {
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  try {
                    Thread.sleep(50);
                  }
                  catch (InterruptedException e) {
                  }
                  openRequestManager.openFiles(files);
                }
              });
            }
          }
        }
      });
      step1 = false;
      File[] file = getInitialFiles();
      synchronized (files) {
        files.addAll(Arrays.asList(file));
      }
      boolean b = nextImport();
      if (b) {
        showStep(panelStep2);
      }
    }
  }

  private boolean nextImport() {

    synchronized (files) {
      if (files.isEmpty()) {
        step2 = false;
      }
    }
    if (completed) {
      return true;
    }
    if (!step2) {
      try {
        completed = true;
        Set<Integer> month = createMonth();
        learn();
        showPositionDialog();
        openRequestManager.popCallback();
        localRepository.commitChanges(true);
        selectLastImportedMonth(month);
        complete();
        return true;
      }
      catch (Exception e) {
        Log.write("nextImport", e);
        return false;
      }
    }

    File file;
    synchronized (files) {
      file = files.remove(0);
    }
    try {
      fileNameLabel.setText(file.getAbsolutePath());
      List<String> dateFormat = importSession.loadFile(file);
      updateActions();
      initQifAccountChooserFields(file);
      initDateFormatSelectionPanel(dateFormat);
      return true;
    }
    catch (Exception e) {
      String message = Lang.get("import.file.error");
      Log.write("", e);
      messageLabel.setText(message);
      return false;
    }
  }

  private void updateActions() {
    currentActions = new ArrayList<AdditionalImportAction>();
    for (AdditionalImportAction action : actions) {
      if (!action.isValid()) {
        currentActions.add(action);
      }
    }
    additionalActionImportRepeat.set(currentActions);
  }

  private void showPositionDialog() {
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

  private Set<Integer> createMonth() {
    localRepository.startChangeSet();
    final SortedSet<Integer> monthIds = new TreeSet<Integer>();
    try {
      localRepository.safeApply(Transaction.TYPE,
                                GlobMatchers.fieldIn(Transaction.IMPORT, importKeys),
                                new GlobFunctor() {
                                  public void run(Glob month, GlobRepository repository) throws Exception {
                                    monthIds.add(month.get(Transaction.BANK_MONTH));
                                    monthIds.add(month.get(Transaction.MONTH));
                                  }
                                });
      if (monthIds.isEmpty()) {
        return monthIds;
      }
      int firstMonth = monthIds.first();
      TimeService time = directory.get(TimeService.class);
      int currentMonth = time.getCurrentMonthId();
      List<Integer> futureMonth = Month.createMonths(firstMonth, currentMonth);
      futureMonth.addAll(Month.createMonths(monthIds.last(), currentMonth));
      for (int month : futureMonth) {
        localRepository.findOrCreate(Key.create(Month.TYPE, month));
      }
    }
    finally {
      localRepository.completeChangeSet();
    }
    return monthIds;
  }

  private void learn() {
    localRepository.safeApply(Transaction.TYPE,
                              GlobMatchers.fieldIn(Transaction.IMPORT, importKeys),
                              new AutoCategorizationFunctor(repository));
  }

  private void selectLastImportedMonth(Set<Integer> month) {
    GlobList monthsToSelect =
      repository.getAll(Month.TYPE, GlobMatchers.fieldIn(Month.ID, month)).sort(Month.ID);
    if (!monthsToSelect.isEmpty()) {
      SelectionService selectionService = directory.get(SelectionService.class);
      selectionService.select(monthsToSelect.getLast());
    }
  }

  private void initDateFormatSelectionPanel(List<String> dateFormats) {
    dateFormatSelectionPanel.init(dateFormats);
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

  private class FinishAction extends AbstractAction {
    public FinishAction() {
      super(Lang.get("import.ok"));
    }

    public void actionPerformed(ActionEvent event) {
      setEnabled(false);
      try {
        messageLabel.setText("");
        if (!dateFormatSelectionPanel.check()) {
          return;
        }
        if (!currentActions.isEmpty()) {
          return;
        }
        Key importKey = importSession.importTransactions(currentlySelectedAccount,
                                                         dateFormatSelectionPanel.getSelectedFormat());
        if (importKey != null) {
          importKeys.add(importKey.get(TransactionImport.ID));
        }
        nextImport();
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
        importSession.discard();
        nextImport();
      }
      finally {
        setEnabled(true);
      }
    }
  }

  private static File[] queryFile(Component parent, File path) {
    JFileChooser chooser = new JFileChooser();
    if (path != null && path.exists()) {
      chooser.setCurrentDirectory(path);
    }
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(true);
    chooser.addChoosableFileFilter(new FileFilter() {
      public boolean accept(File file) {
        return BankFileType.isFileNameSupported(file.getName())
               || file.isDirectory();
      }

      public String getDescription() {
        return Lang.get("bank.file.format");
      }
    });
    int returnVal = chooser.showOpenDialog(parent);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File[] selectedFiles = chooser.getSelectedFiles();
      if (selectedFiles == null || selectedFiles.length == 0) {
        Log.write("import : no file selected ");
      }
      else {
        for (File selectedFile : selectedFiles) {
          if (!selectedFile.exists()) {
            Log.write("import : Error: file " + selectedFile.getName() + " not found");
          }
        }
      }
      return selectedFiles;
    }
    return null;
  }

  private class BrowseFilesAction extends AbstractAction {
    private BrowseFilesAction() {
      super(Lang.get("browse"));
    }

    public void actionPerformed(ActionEvent e) {
      File path = null;
      if (usePreferedPath) {
        Glob preferences = localRepository.get(UserPreferences.KEY);
        String directory = preferences.get(UserPreferences.LAST_IMPORT_DIRECTORY);
        if (directory != null) {
          path = new File(directory);
        }
      }
      File[] files = queryFile(dialog, path);
      if (files != null) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < files.length; i++) {
          buffer.append(files[i].getPath());
          if (i + 1 < files.length) {
            buffer.append(";");
          }
          if (usePreferedPath) {
            localRepository.update(UserPreferences.KEY, UserPreferences.LAST_IMPORT_DIRECTORY,
                                   files[i].getAbsoluteFile().getParent());
          }
        }
        fileField.setText(buffer.toString());
      }
    }
  }

  private class ImportedTransactionDateRenderer implements LabelCustomizer {
    private GlobTableView transactionTable;
    private SimpleDateFormat format;

    public void changeDateFormat(String dateFormat) {
      if (dateFormat == null) {
        format = null;
      }
      else {
        format = new SimpleDateFormat(dateFormat);
      }
      transactionTable.refresh();
    }

    public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
      if (format == null) {
        label.setText(glob.get(ImportedTransaction.BANK_DATE));
        return;
      }
      Date date = null;
      try {
        date = format.parse(glob.get(ImportedTransaction.BANK_DATE));
      }
      catch (ParseException e) {
        label.setText("Failed to parse date");
      }
      label.setText(Formatting.toString(date));
    }

    public void setTable(GlobTableView transactionTable) {
      this.transactionTable = transactionTable;
    }

    public Comparator<Glob> getComparator() {
      return new Comparator<Glob>() {
        public int compare(Glob o1, Glob o2) {
          if (format == null) {
            return Utils.compare(o1.get(ImportedTransaction.BANK_DATE), o2.get(ImportedTransaction.BANK_DATE));
          }
          try {
            Date date1 = format.parse(o1.get(ImportedTransaction.BANK_DATE));
            Date date2 = format.parse(o2.get(ImportedTransaction.BANK_DATE));
            int compareResult = date2.compareTo(date1);
            if (compareResult == 0) {
              Integer id2 = o2.get(ImportedTransaction.ID);
              Integer id1 = o1.get(ImportedTransaction.ID);
              return Utils.compare(id2, id1);
            }
            return compareResult;
          }
          catch (ParseException e) {
            return Utils.compare(o1.get(ImportedTransaction.BANK_DATE), o2.get(ImportedTransaction.BANK_DATE));
          }
        }
      };
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction(String textForCloseButton) {
      super(textForCloseButton);
    }

    public void actionPerformed(ActionEvent e) {
      complete();
    }
  }

  private static class TransactionLabelGlobStringifier extends AbstractGlobStringifier {

    public String toString(Glob glob, GlobRepository repository) {
      if (glob.isTrue(ImportedTransaction.IS_OFX)) {
        StringBuilder builder = new StringBuilder();
        complete(builder, glob.get(ImportedTransaction.OFX_NAME));
        complete(builder, glob.get(ImportedTransaction.OFX_CHECK_NUM));
        complete(builder, glob.get(ImportedTransaction.OFX_MEMO));
        return builder.toString();
      }
      else {
        StringBuilder builder = new StringBuilder();
        complete(builder, glob.get(ImportedTransaction.QIF_M));
        complete(builder, glob.get(ImportedTransaction.QIF_P));
        return builder.toString();
      }

    }

    void complete(StringBuilder builder, String s) {
      if (s == null) {
        return;
      }
      if (builder.length() != 0) {
        builder.append(":");
      }
      builder.append(s);
    }
  }
}