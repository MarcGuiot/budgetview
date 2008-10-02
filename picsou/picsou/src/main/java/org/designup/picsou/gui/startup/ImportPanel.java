package org.designup.picsou.gui.startup;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.importer.BankFileType;
import org.designup.picsou.importer.ImportSession;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.AbstractDocumentListener;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import static org.globsframework.gui.views.utils.LabelCustomizers.chain;
import static org.globsframework.gui.views.utils.LabelCustomizers.fontSize;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
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
  private LocalGlobRepository localRepository;
  private Directory localDirectory;

  private DefaultDirectory sessionDirectory;
  private ImportSession importSession;
  private final List<File> files = new ArrayList<File>();
  private Glob currentlySelectedAccount;

  private AccountEditionPanel accountEditionPanel;
  private BankEntityEditionPanel bankEntityEditionPanel;
  private DateFormatSelectionPanel dateFormatSelectionPanel;

  private JButton newAccountButton;
  private JComboBox accountComboBox;
  private JLabel importMessageLabel = new JLabel();
  private GlobRepository sessionRepository;
  private ImportedTransactionDateRenderer dateRenderer;
  private boolean step1 = true;
  private boolean step2 = true;
  private OpenRequestManager openRequestManager;
  private Glob defaultAccount;
  private Window owner;
  private Set<Integer> importKeys = new HashSet<Integer>();
  private JPanel mainPanel;
  private JPanel panelStep1;
  private JPanel panelStep2;

  private static final int[] COLUMN_SIZES = {10, 45};

  private PicsouDialog dialog;

  public ImportPanel(String textForCloseButton, List<File> files, Glob defaultAccount,
                     final Window owner, final GlobRepository repository, Directory directory) {

    this.defaultAccount = defaultAccount;
    this.owner = owner;
    this.repository = repository;
    this.directory = directory;

    updateFileField(files);
    initOpenRequestManager(directory);
    loadLocalRepository(repository);

    this.localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());

    dialog = PicsouDialog.create(owner, directory);
    
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
    mainPanel.setLayout(new GridBagBuilder.UniqueComponentLayoutManager(null));
    mainPanel.add(panelStep1);
  }

  private void initStep1Panel(String textForCloseButton, Directory directory) {
    fileButton.setAction(new BrowseFilesAction());

    initFileField();

    GlobsPanelBuilder builder1 = new GlobsPanelBuilder(getClass(), "/layout/importPanelStep1.splits", localRepository, localDirectory);
    builder1.add("message", messageLabel);
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
                                   selection.getAll(Account.TYPE).get(0);
      }
    }, Account.TYPE);

    importSession = new ImportSession(localRepository, sessionDirectory);
    sessionRepository = importSession.getTempRepository();

    GlobTableView tableView = GlobTableView.init(ImportedTransaction.TYPE, sessionRepository,
                                                 dateRenderer.getComparator(), sessionDirectory)
      .addColumn(Lang.get("import.bankDate"), ImportedTransaction.BANK_DATE,
                 chain(fontSize(9), dateRenderer))
      .addColumn(Lang.get("label"), ImportedTransaction.LABEL)
      .addColumn(Lang.get("amount"), ImportedTransaction.AMOUNT);

    PicsouTableHeaderPainter.install(tableView, localDirectory);

    JTable transactionTable = tableView.getComponent();
    dateRenderer.setTable(tableView);
    Gui.setColumnSizes(transactionTable, COLUMN_SIZES);

    builder2.add("table", transactionTable);
    builder2.add("fileName", fileNameLabel);

    newAccountButton = new JButton(new NewAccountAction(sessionRepository, sessionDirectory, dialog));
    builder2.add("newAccount", newAccountButton);

    GlobComboView comboView = GlobComboView.init(Account.TYPE, sessionRepository, sessionDirectory);
    accountComboBox = comboView.getComponent();
    builder2.add("accountCombo", accountComboBox);
    comboView.setFilter(new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return item != null && !item.get(Account.ID).equals(Account.SUMMARY_ACCOUNT_ID);
      }
    });

    bankEntityEditionPanel = new BankEntityEditionPanel(sessionRepository, sessionDirectory, importMessageLabel);
    builder2.add("bankEntityEditionPanel", bankEntityEditionPanel.getPanel());

    accountEditionPanel = new AccountEditionPanel(sessionRepository, sessionDirectory, importMessageLabel);
    builder2.add("accountEditionPanel", accountEditionPanel.getBuilder());

    builder2.add("importMessage", importMessageLabel);

    builder2.add("skipFile", new SkipFileAction());
    builder2.add("finish", new FinishAction());
    builder2.add("close", new CancelAction(textForCloseButton));
    this.panelStep2 = builder2.load();
  }

  private void initOpenRequestManager(Directory directory) {
    openRequestManager = directory.get(OpenRequestManager.class);
    openRequestManager.pushCallback(new OpenRequestManager.Callback() {
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
    GlobType[] globTypes = {Bank.TYPE, BankEntity.TYPE, Account.TYPE, Category.TYPE, Transaction.TYPE,
                            TransactionTypeMatcher.TYPE, Month.TYPE};
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
          GuiUtils.showCentered(dialog);
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
      GuiUtils.showCentered(dialog);
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
        public void openFiles(List<File> files) {
          synchronized (ImportPanel.this.files) {
            if (step2) {
              ImportPanel.this.files.addAll(files);
            }
            else {
              openRequestManager.openFiles(files);
            }
          }
        }
      });
      step1 = false;
      File[] file = getInitialFiles();
      synchronized (files) {
        files.addAll(Arrays.asList(file));
      }
      if (nextImport()) {
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
    if (!step2) {
      try {
        openRequestManager.popCallback();
        Set<Integer> month = createMonth();
        learn();
        localRepository.commitChanges(true);
        selectImportedMonth(month);
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
      initBankEntityEditionPanel();
      initCreationAccountFields(file);
      initDateFormatSelectionPanel(dateFormat);
      return true;
    }
    catch (Exception e) {
      String message = Lang.get("import.file.error", file.getAbsolutePath());
      Log.write("", e);
      messageLabel.setText(message);
      return false;
    }
  }

  private Set<Integer> createMonth() {
    localRepository.enterBulkDispatchingMode();
    final SortedSet<Integer> monthIds = new TreeSet<Integer>();
    try {
      localRepository.saveApply(Transaction.TYPE,
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
      localRepository.completeBulkDispatchingMode();
    }
    return monthIds;
  }

  private void learn() {
    localRepository.saveApply(Transaction.TYPE,
                              GlobMatchers.fieldIn(Transaction.IMPORT, importKeys),
                              new LearningGlobFunctor(repository));
  }

  private void selectImportedMonth(Set<Integer> month) {
    GlobList monthsToSelect = repository.getAll(Month.TYPE, GlobMatchers.fieldIn(Month.ID, month));
    if (!monthsToSelect.isEmpty()) {
      SelectionService selectionService = directory.get(SelectionService.class);
      selectionService.select(monthsToSelect, Month.TYPE);
    }
  }

  private void initDateFormatSelectionPanel(List<String> dateFormats) {
    dateFormatSelectionPanel.init(dateFormats);
  }

  private void initBankEntityEditionPanel() {
    GlobList entities = sessionRepository.getAll(BankEntity.TYPE, GlobMatchers.isNull(BankEntity.BANK));
    bankEntityEditionPanel.init(entities);
  }

  private void initCreationAccountFields(File file) {
    if (BankFileType.getTypeFromName(file.getAbsolutePath()).equals(BankFileType.QIF)) {
      GlobList accounts = sessionRepository.getAll(Account.TYPE);
      if (accounts.size() == 1 && accounts.get(0).get(Account.ID).equals(Account.SUMMARY_ACCOUNT_ID)) {
        Glob createdAccount = importSession.createDefaultAccount();
        accountEditionPanel.setAccount(createdAccount);
        accountComboBox.setVisible(false);
        newAccountButton.setVisible(false);
        sessionDirectory.get(SelectionService.class).select(createdAccount);
      }
      else {
        Glob account = null;
        if (defaultAccount != null) {
          account = sessionRepository.get(defaultAccount.getKey());
        }
        else if (accounts.size() == 2) {
          if (accounts.get(0).get(Account.ID).equals(Account.SUMMARY_ACCOUNT_ID)) {
            account = accounts.get(1);
          }
          else {
            account = accounts.get(0);
          }
        }
        accountComboBox.setVisible(true);
        newAccountButton.setVisible(true);
        accountEditionPanel.setAccount(null);
        if (account != null) {
          sessionDirectory.get(SelectionService.class).select(account);
        }
      }
    }
    else {
      accountEditionPanel.setAccount(null);
      accountComboBox.setVisible(false);
      newAccountButton.setVisible(false);
    }
  }

  private class FinishAction extends AbstractAction {
    public FinishAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent event) {
      setEnabled(false);
      try {
        messageLabel.setText("");
        if (!dateFormatSelectionPanel.check()) {
          return;
        }
        if (!bankEntityEditionPanel.check()) {
          return;
        }
        if (!accountEditionPanel.check()) {
          return;
        }
        Key importKey = importSession.importTransactions(currentlySelectedAccount,
                                                         dateFormatSelectionPanel.getSelectedFormat());
        importKeys.add(importKey.get(TransactionImport.ID));
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

  private File[] queryFile(Component parent) {
    JFileChooser chooser = new JFileChooser();
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
        System.out.println("no file selected ");
      }
      else {
        for (File selectedFile : selectedFiles) {
          if (!selectedFile.exists()) {
            System.out.println("Error: file " + selectedFile.getName() + " not found");
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
      File[] files = queryFile(owner.getOwner());
      if (files != null) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < files.length; i++) {
          buffer.append(files[i].getPath());
          if (i + 1 < files.length) {
            buffer.append(";");
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
      label.setText(PicsouDescriptionService.toString(date));
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
}