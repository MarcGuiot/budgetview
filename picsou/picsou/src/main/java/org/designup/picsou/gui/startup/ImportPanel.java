package org.designup.picsou.gui.startup;

import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.components.DialogOwner;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.importer.BankFileType;
import org.designup.picsou.importer.ImportSession;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.actions.AbstractGlobSelectionAction;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.CellPainter;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public abstract class ImportPanel {
  private JLabel messageLabel = new JLabel();
  private JPanel filePanel = new JPanel();
  private final JTextField fileField = new JTextField();
  private JButton fileButton = new JButton();
  private JLabel fileNameLabel = new JLabel();

  private ImportSession importSession;
  private final List<File> files = new ArrayList<File>();
  private LocalGlobRepository localRepository;
  private Directory localDirectory;
  private AccountEditionPanel accountEditionPanel;
  private BankEntityEditionPanel bankEntityEditionPanel;
  private DateFormatSelectionPanel dateFormatSelectionPanel;
  private Glob bank;
  private JButton newAccountButton;
  private JComboBox accountComboBox;
  private JLabel importMessageLabel = new JLabel();
  private Glob currentlySelectedAccount;
  private DefaultDirectory sessionDirectory;
  private GlobRepository sessionRepository;
  private ImportedTransactionDateRenderer dateRenderer;
  private boolean step1 = true;
  private boolean step2 = true;
  private OpenRequestManager openRequestManager;
  private Glob defaultAccount;
  private DialogOwner owner;
  private GlobRepository repository;
  private Directory directory;
  private Set<Integer> importKeys = new HashSet<Integer>();
  private JPanel mainPanel;
  private JPanel panelStep1;
  private JPanel panelStep2;
  private GlobsPanelBuilder mainBuilder;

  protected ImportPanel(String textForCloseButton, List<File> files, Glob defaultAccount,
                        final DialogOwner owner, final GlobRepository repository, Directory directory) {
    this.defaultAccount = defaultAccount;
    this.owner = owner;
    this.repository = repository;
    this.directory = directory;

    updateFileField(files);
    initOpenRequestManager(directory);
    loadLocalRepository(repository);

    this.localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());

    initStep1Panel(textForCloseButton, directory);
    initStep2Panel(textForCloseButton, owner);
    initMainPanel();

    if (defaultAccount != null) {
      Glob bank = Account.getBank(defaultAccount, localRepository);
      localDirectory.get(SelectionService.class).select(bank);
    }
  }

  private void initMainPanel() {
    mainBuilder = new GlobsPanelBuilder(getClass(), "/layout/importPanel.splits", localRepository, localDirectory);
    mainPanel = new JPanel();
    mainBuilder.add("mainPanel", mainPanel);
    mainBuilder.addLoader(new SplitsLoader() {
      public void load(Component component) {
        showStep(panelStep1);
      }
    });
    mainBuilder.load();
  }

  private void initStep1Panel(String textForCloseButton, Directory directory) {
    fileButton.setAction(new BrowseFilesAction());

    GlobsPanelBuilder builder1 = new GlobsPanelBuilder(getClass(), "/layout/importPanelStep1.splits", localRepository, localDirectory);
    builder1.add("message", messageLabel);
    builder1.add("filePanel", filePanel);
    builder1.add("fileField", fileField);
    builder1.add("fileButton", fileButton);
    builder1.add("bankCombo",
                 GlobComboView.init(Bank.TYPE, localRepository, localDirectory).setShowEmptyOption(true).getComponent());
    builder1.add("downloadUrl", new DownloadAction(directory));

    builder1.add("import", new ImportAction());
    builder1.add("close", new AbstractAction(textForCloseButton) {
      public void actionPerformed(ActionEvent e) {
        openRequestManager.popCallback();
        complete();
      }
    });

    panelStep1 = builder1.load();
  }

  private void initStep2Panel(final String textForCloseButton, DialogOwner owner) {
    localDirectory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList banks = selection.getAll();
        bank = banks.isEmpty() ? null : banks.get(0);
      }
    }, Bank.TYPE);

    GlobsPanelBuilder builder2 = new GlobsPanelBuilder(getClass(), "/layout/importPanelStep2.splits", localRepository, localDirectory);
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
        currentlySelectedAccount = selection.getAll().isEmpty() ? null : selection.getAll().get(0);
      }
    }, Account.TYPE);

    importSession = new ImportSession(localRepository, sessionDirectory);
    sessionRepository = importSession.getTempRepository();

    GlobTableView importedTransactionTableView = GlobTableView.init(ImportedTransaction.TYPE, sessionRepository,
                                                                    dateRenderer.getComparator(), sessionDirectory)
      .addColumn(ImportedTransaction.BANK_DATE, dateRenderer, CellPainter.NULL)
      .addColumn(ImportedTransaction.LABEL)
      .addColumn(ImportedTransaction.AMOUNT);
    JTable transactionTable = importedTransactionTableView.getComponent();
    dateRenderer.setTable(importedTransactionTableView);

    builder2.add("table", transactionTable);
    builder2.add("fileName", fileNameLabel);

    newAccountButton = new JButton(new NewAccountAction(sessionRepository, sessionDirectory, owner));
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
    builder2.add("close", new AbstractAction(textForCloseButton) {
      public void actionPerformed(ActionEvent e) {
        complete();
      }
    });
    builder2.add("back", new AbstractAction("back") {
      public void actionPerformed(ActionEvent e) {
        step1 = true;
        step2 = true;
        ImportPanel.this.files.clear();
        ImportPanel.this.fileField.setText("");
        importSession.discard();
        loadLocalRepository(ImportPanel.this.repository);
        showStep(panelStep1);
      }
    });

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
    GridBagBuilder.setSingleCell(mainPanel, step);
    mainPanel.validate();
    JDialog dialog = GuiUtils.getEnclosingComponent(mainPanel.getParent(), JDialog.class);
    if (dialog != null) {
      dialog.pack();
      GuiUtils.center(dialog);
    }
  }

  private void loadLocalRepository(GlobRepository repository) {
    GlobType[] globTypes = {Bank.TYPE, BankEntity.TYPE, Account.TYPE, Category.TYPE, Transaction.TYPE,
                            TransactionToCategory.TYPE, TransactionTypeMatcher.TYPE, LabelToCategory.TYPE};
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

  protected abstract void complete();

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

  public GlobsPanelBuilder getBuilder() {
    return mainBuilder;
  }

  private class ImportAction extends AbstractAction {
    public ImportAction() {
      super("Import");
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
        localRepository.commitChanges(true);
        selectImportedMonth();
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
      if (bank != null) {
        sessionDirectory.get(SelectionService.class).select(sessionRepository.get(bank.getKey()));
      }
      return true;
    }
    catch (Exception e) {
      String message = Lang.get("import.file.error", file.getAbsolutePath());
      Log.write("", e);
      messageLabel.setText(message);
      return false;
    }
  }

  private void selectImportedMonth() {
    repository.enterBulkDispatchingMode();
    Set<Integer> monthIds;
    try {
      monthIds = repository.getAll(Transaction.TYPE,
                                   GlobMatchers.fieldIn(Transaction.IMPORT, importKeys)).getValueSet(Transaction.BANK_MONTH);
      for (Integer monthId : monthIds) {
        repository.findOrCreate(Key.create(Month.TYPE, monthId));
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
    GlobList monthToSelect = repository.getAll(Month.TYPE, GlobMatchers.fieldIn(Month.ID, monthIds));
    if (!monthToSelect.isEmpty()) {
      directory.get(SelectionService.class).select(monthToSelect, Month.TYPE);
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
        accountEditionPanel.setAccount(createdAccount, bank == null ? null : sessionRepository.get(bank.getKey()));
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
        accountEditionPanel.setAccount(null, null);
        if (account != null) {
          sessionDirectory.get(SelectionService.class).select(account);
        }
      }
    }
    else {
      accountEditionPanel.setAccount(null, null);
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
        Key importKey = importSession.importTransactions(currentlySelectedAccount, dateFormatSelectionPanel.getSelectedFormat());
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
        return file.getName().endsWith("ofx") || file.getName().endsWith("qif") || file.isDirectory();
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
            System.out.println("erreur : file " + selectedFile.getName() + " not found");
          }
        }
      }
      return selectedFiles;
    }
    return null;
  }

  private class BrowseFilesAction extends AbstractAction {
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

  private class DownloadAction extends AbstractGlobSelectionAction {
    private Directory directory;

    public DownloadAction(Directory directory) {
      super(Bank.TYPE, ImportPanel.this.localDirectory);
      this.directory = directory;
    }

    public String toString(GlobList globs) {
      if (globs.size() != 1) {
        return null;
      }
      return globs.get(0).get(Bank.DOWNLOAD_URL);
    }

    public void actionPerformed(ActionEvent e) {
      directory.get(BrowsingService.class).launchBrowser(getName());
    }
  }
}