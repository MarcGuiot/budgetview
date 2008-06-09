package org.designup.picsou.gui;

import org.crossbowlabs.globs.gui.views.GlobTableView;
import org.crossbowlabs.globs.gui.views.GlobComboView;
import org.crossbowlabs.globs.gui.views.GlobHtmlView;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.format.GlobListStringifier;
import org.crossbowlabs.globs.utils.Strings;
import org.crossbowlabs.globs.utils.Log;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.SplitsBuilder;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.components.JStyledPanel;
import org.crossbowlabs.splits.layout.CardHandler;
import org.designup.picsou.gui.transactions.TransactionDateStringifier;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.importer.ImportSession;
import org.designup.picsou.importer.BankFileType;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.Bank;
import static org.designup.picsou.model.Transaction.*;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.File;
import java.io.IOException;
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

  protected ImportPanel(GlobRepository repository, Directory directory) {
    fileButton.setAction(new BrowseFilesAction());

    importSession = new ImportSession(repository, directory);

    TransactionComparator comparator = new TransactionComparator(false);

    JTable transactionTable =
      GlobTableView.init(Transaction.TYPE, importSession.getTempRepository(), comparator, directory)
        .addColumn(Lang.get("date"), new TransactionDateStringifier(comparator))
        .addColumn(LABEL)
        .addColumn(AMOUNT)
        .getComponent();

    ColorService colorService = PicsouColors.createColorService();
    SplitsBuilder builder = new SplitsBuilder(colorService, Gui.ICON_LOCATOR, Lang.TEXT_LOCATOR);
    builder.add("message", messageLabel);
    builder.add("filePanel", filePanel);
    builder.add("fileLabel", fileLabel);
    builder.add("fileField", fileField);
    builder.add("fileButton", fileButton);
    builder.add("bankCombo",
                GlobComboView.init(Bank.TYPE, repository, directory).getComponent());
    builder.add("downloadUrl",
                GlobHtmlView.init(Bank.TYPE, repository, directory, new BankUrlStringifier()).getComponent());
    builder.add("import", new ImportAction());
    builder.add("table", transactionTable);
    builder.add("fileName", fileNameLabel);
    builder.add("finish", new FinishAction());
    builder.add("close", new AbstractAction("Fermer") {
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
      return true;
    }
    catch (Exception e) {
      String message = Lang.get("import.file.error", file.getAbsolutePath());
      messageLabel.setText(message);
      return false;
    }

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
      return "<a href='"+ url +"'>"+url+"</a>";
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