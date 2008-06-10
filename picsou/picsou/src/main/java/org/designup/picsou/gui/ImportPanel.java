package org.designup.picsou.gui;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.gui.views.GlobComboView;
import org.crossbowlabs.globs.gui.views.GlobHtmlView;
import org.crossbowlabs.globs.gui.views.GlobTableView;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.GlobListStringifier;
import org.crossbowlabs.globs.model.utils.LocalGlobRepository;
import org.crossbowlabs.globs.model.utils.LocalGlobRepositoryBuilder;
import org.crossbowlabs.globs.utils.Log;
import org.crossbowlabs.globs.utils.Strings;
import org.crossbowlabs.globs.utils.directory.DefaultDirectory;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.SplitsBuilder;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.components.JStyledPanel;
import org.crossbowlabs.splits.layout.CardHandler;
import org.crossbowlabs.splits.utils.GuiUtils;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.transactions.TransactionDateStringifier;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.importer.BankFileType;
import org.designup.picsou.importer.ImportSession;
import org.designup.picsou.model.*;
import static org.designup.picsou.model.Transaction.*;
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
  private JLabel fileLabel = new JLabel();
  private JTextField fileField = new JTextField();
  private JButton fileButton = new JButton();
  private JPanel panel;
  protected CardHandler cardHandler;
  private JLabel fileNameLabel = new JLabel();

  private ImportSession importSession;
  private List<File> files = new ArrayList();
  private LocalGlobRepository localRepository;
  private Directory localDirectory;
  protected AccountEditionPanel accountEditionPanel;
  private Glob bank;
  protected JButton newAccountButton;
  protected JComboBox accountComboBox;

  protected ImportPanel(final Window owner, GlobRepository repository, Directory directory) {

    this.localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Bank.TYPE, BankEntity.TYPE, Account.TYPE, Category.TYPE, Transaction.TYPE,
            TransactionToCategory.TYPE, TransactionTypeMatcher.TYPE, LabelToCategory.TYPE)
      .get();

    this.localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());
    fileButton.setAction(new BrowseFilesAction());

    importSession = new ImportSession(localRepository, localDirectory);

    TransactionComparator comparator = new TransactionComparator(false);

    GlobRepository tempRepository = importSession.getTempRepository();
    JTable transactionTable =
      GlobTableView.init(Transaction.TYPE, tempRepository, comparator, localDirectory)
        .addColumn(Lang.get("date"), new TransactionDateStringifier(comparator))
        .addColumn(LABEL)
        .addColumn(AMOUNT)
        .getComponent();

    ColorService colorService = localDirectory.get(ColorService.class);
    SplitsBuilder builder = new SplitsBuilder(colorService, Gui.ICON_LOCATOR, Lang.TEXT_LOCATOR);
    builder.add("message", messageLabel);
    builder.add("filePanel", filePanel);
    builder.add("fileLabel", fileLabel);
    builder.add("fileField", fileField);
    builder.add("fileButton", fileButton);
    builder.add("bankCombo",
                GlobComboView.init(Bank.TYPE, localRepository, localDirectory).setShowEmptyOption(true).getComponent());
    builder.add("downloadUrl",
                GlobHtmlView.init(Bank.TYPE, localRepository, localDirectory, new BankUrlStringifier()).getComponent());
    builder.add("import", new ImportAction());
    builder.add("table", transactionTable);
    builder.add("fileName", fileNameLabel);

    newAccountButton = new JButton(new NewAccountAction(owner));
    builder.add("newAccount", newAccountButton);

    accountComboBox = GlobComboView.init(Account.TYPE, localRepository, localDirectory).setShowEmptyOption(true).getComponent();
    builder.add("accountCombo", accountComboBox);

    accountEditionPanel = new AccountEditionPanel(localRepository, localDirectory);
    builder.add("accountEditionPanel", accountEditionPanel.getPanel());
    builder.add("finish", new FinishAction());
    builder.add("close", new AbstractAction(Lang.get("close")) {
      public void actionPerformed(ActionEvent e) {
        complete();
      }
    });
    cardHandler = builder.addCardHandler("cardHandler");
    panel = (JPanel)builder.parse(getClass(), "/layout/importPanel.splits");

    localDirectory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList banks = selection.getAll();
        bank = banks.isEmpty() ? null : banks.get(0);
      }
    }, Bank.TYPE);
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
      initCreationAccountFields(file);
      fileNameLabel.setText(file.getAbsolutePath());
      importSession.loadFile(file);
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
    Glob account = null;
    if (BankFileType.getTypeFromName(file.getAbsolutePath()).equals(BankFileType.QIF)) {
      GlobList accounts = localRepository.getAll(Account.TYPE);
      if (accounts.size() == 1) {
        if (!accounts.get(0).get(Account.ID).equals(Account.SUMMARY_ACCOUNT_ID)) {
          account = localRepository.create(Account.TYPE);
        }
      }
    }
    accountEditionPanel.setAccount(account, bank);
    accountComboBox.setVisible(account != null);
    newAccountButton.setVisible(account != null);
  }

  private class FinishAction extends AbstractAction {
    public FinishAction() {
      super("OK");
    }

    public void actionPerformed(ActionEvent event) {
      importSession.importTransactions();
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

  private class NewAccountAction extends AbstractAction {
    private final Window owner;

    public NewAccountAction(Window owner) {
      super(Lang.get("new.account"));
      this.owner = owner;
    }

    public void actionPerformed(ActionEvent e) {
      final LocalGlobRepository tempRespository = LocalGlobRepositoryBuilder.init(localRepository)
        .copy(Bank.TYPE, BankEntity.TYPE).get();
      AccountEditionPanel accountEditionPanel = new AccountEditionPanel(tempRespository, localDirectory);
      PicsouDialog dialog =
        PicsouDialog.createWithButtons(owner, accountEditionPanel.getPanel(),
                                       new AbstractAction(Lang.get("ok")) {
                                         public void actionPerformed(ActionEvent e) {
                                           tempRespository.commitChanges(true);
                                         }
                                       });
      dialog.pack();
      GuiUtils.showCentered(dialog);
    }
  }
}