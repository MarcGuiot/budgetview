package org.designup.picsou.gui;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.GlobsPanelBuilder;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.gui.views.GlobComboView;
import org.crossbowlabs.globs.gui.views.GlobHtmlView;
import org.crossbowlabs.globs.gui.views.GlobTableView;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.GlobListStringifier;
import org.crossbowlabs.globs.model.utils.GlobMatcher;
import org.crossbowlabs.globs.model.utils.LocalGlobRepository;
import org.crossbowlabs.globs.model.utils.LocalGlobRepositoryBuilder;
import org.crossbowlabs.globs.utils.Log;
import org.crossbowlabs.globs.utils.Strings;
import org.crossbowlabs.globs.utils.directory.DefaultDirectory;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.components.JStyledPanel;
import org.crossbowlabs.splits.layout.CardHandler;
import org.designup.picsou.gui.transactions.TransactionDateStringifier;
import org.designup.picsou.importer.BankFileType;
import org.designup.picsou.importer.ImportSession;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ImportPanel {
  private JLabel messageLabel = new JLabel();
  private JStyledPanel filePanel = new JStyledPanel();
  private JTextField fileField = new JTextField();
  private JButton fileButton = new JButton();
  private JPanel panel;
  protected CardHandler cardHandler;
  private JLabel fileNameLabel = new JLabel();

  private ImportSession importSession;
  private List<File> files = new ArrayList<File>();
  private LocalGlobRepository localRepository;
  private Directory localDirectory;
  protected AccountEditionPanel accountEditionPanel;
  private Glob bank;
  protected JButton newAccountButton;
  protected JComboBox accountComboBox;
  private Glob currentlySelectedAccount;
  private DefaultDirectory sessionDirectory;
  private GlobRepository sessionRepository;

  protected ImportPanel(final Window owner, GlobRepository repository, Directory directory) {

    this.localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Bank.TYPE, BankEntity.TYPE, Account.TYPE, Category.TYPE, Transaction.TYPE,
            TransactionToCategory.TYPE, TransactionTypeMatcher.TYPE, LabelToCategory.TYPE)
      .get();

    this.localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());
    fileButton.setAction(new BrowseFilesAction());


    GlobsPanelBuilder builder = new GlobsPanelBuilder(localRepository, localDirectory);
    //Step 1
    builder.add("message", messageLabel);
    builder.add("filePanel", filePanel);
    builder.add("fileField", fileField);
    builder.add("fileButton", fileButton);
    builder.add("bankCombo",
                GlobComboView.init(Bank.TYPE, localRepository, localDirectory).setShowEmptyOption(true).getComponent());
    builder.add("downloadUrl",
                GlobHtmlView.init(Bank.TYPE, localRepository, localDirectory, new BankUrlStringifier()).getComponent());
    builder.add("import", new ImportAction());

    localDirectory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList banks = selection.getAll();
        bank = banks.isEmpty() ? null : banks.get(0);
      }
    }, Bank.TYPE);

    //step 2
    sessionDirectory = new DefaultDirectory(localDirectory);
    SelectionService selectionService = new SelectionService();
    sessionDirectory.add(selectionService);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentlySelectedAccount = selection.getAll().isEmpty() ? null : selection.getAll().get(0);
      }
    }, Account.TYPE);

    importSession = new ImportSession(localRepository, sessionDirectory);
    sessionRepository = importSession.getTempRepository();

    TransactionComparator comparator = TransactionComparator.DESCENDING_BANK;
    TransactionDateStringifier dateStringifier =
      new TransactionDateStringifier(comparator, Transaction.BANK_MONTH, Transaction.BANK_DAY);
    JTable transactionTable =
      GlobTableView.init(Transaction.TYPE, sessionRepository, comparator, sessionDirectory)
        .addColumn(Lang.get("date"), dateStringifier)
        .addColumn(Transaction.LABEL)
        .addColumn(Transaction.AMOUNT)
        .getComponent();

    builder.add("table", transactionTable);
    builder.add("fileName", fileNameLabel);

    newAccountButton = new JButton(new NewAccountAction(sessionRepository, sessionDirectory, owner));
    builder.add("newAccount", newAccountButton);

    GlobComboView comboView = GlobComboView.init(Account.TYPE, sessionRepository, sessionDirectory);
    accountComboBox = comboView.getComponent();
    builder.add("accountCombo", accountComboBox);
    comboView.setFilter(new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return item != null && !item.get(Account.ID).equals(Account.SUMMARY_ACCOUNT_ID);
      }
    });

    accountEditionPanel = new AccountEditionPanel(sessionRepository, sessionDirectory);
    builder.add("accountEditionPanel", accountEditionPanel.getPanel());
    builder.add("skipFile", new SkipFileAction());
    builder.add("finish", new FinishAction());
    builder.add("close", new AbstractAction(Lang.get("close")) {
      public void actionPerformed(ActionEvent e) {
        complete();
      }
    });
    cardHandler = builder.addCardHandler("cardHandler");
    panel = (JPanel)builder.parse(getClass(), "/layout/importPanel.splits");

  }

  protected abstract void complete();

  private File[] getInitialFiles() {
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

  public JPanel getPanel() {
    return panel;
  }

  private class ImportAction extends AbstractAction {
    public ImportAction() {
      super("Import");
    }

    public void actionPerformed(ActionEvent event) {
      if (!initialFileAccepted()) {
        return;
      }
      File[] file = getInitialFiles();
      files.addAll(Arrays.asList(file));
      if (nextImport()) {
        cardHandler.show("step2");
      }
    }
  }

  private boolean nextImport() {
    if (files.isEmpty()) {
      try {
        localRepository.commitChanges(true);
        complete();
        return true;
      }
      catch (Exception e) {
        displayErrorMessage("");
        return false;
      }
    }

    File file = files.remove(0);
    try {

      fileNameLabel.setText(file.getAbsolutePath());
      importSession.loadFile(file);
      initCreationAccountFields(file);
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
        if (accounts.size() == 2) {
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
      importSession.importTransactions(currentlySelectedAccount);
      nextImport();
    }
  }

  private class SkipFileAction extends AbstractAction {
    private SkipFileAction() {
      super(Lang.get("import.skip.file"));
    }

    public void actionPerformed(ActionEvent e) {
      importSession.discard();
      nextImport();
    }
  }

  private static class BankUrlStringifier implements GlobListStringifier {
    public String toString(GlobList selected) {
      if (selected.size() != 1) {
        return "";
      }
      Glob bank = selected.get(0);
      String url = bank.get(Bank.DOWNLOAD_URL);
      return "<a href='" + url + "'>" + url + "</a>";
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
      File[] files = queryFile(panel);
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
}