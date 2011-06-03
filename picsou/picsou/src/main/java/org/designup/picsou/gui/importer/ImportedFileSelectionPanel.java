package org.designup.picsou.gui.importer;

import org.designup.picsou.gui.components.dialogs.MessageAndDetailsDialog;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.importer.components.BankDownloadPanel;
import org.designup.picsou.gui.importer.edition.BrowseFilesAction;
import org.designup.picsou.importer.utils.TypedInputStream;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.utils.AbstractDocumentListener;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImportedFileSelectionPanel {

  private ImportController controller;
  private LocalGlobRepository localRepository;
  private Directory directory;

  private JPanel panel;
  private JPanel filePanel = new JPanel();
  private GlobsPanelBuilder builder;
  private final JTextField fileField;

  private boolean usePreferredPath;
  private JEditorPane importMessage = new JEditorPane();
  private Exception lastException;
  private BankDownloadPanel bankDownload;

  public ImportedFileSelectionPanel(ImportController controller,
                                    boolean usePreferredPath,
                                    LocalGlobRepository localRepository,
                                    Directory directory) {
    this.controller = controller;
    this.usePreferredPath = usePreferredPath;
    this.localRepository = localRepository;
    this.directory = directory;
    this.fileField = controller.getFileField();
  }

  public void init(final PicsouDialog dialog, String textForCloseButton) {

    initFileField();

    builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importFileSelectionPanel.splits",
                                    localRepository, directory);
    builder.add("importMessage", importMessage);
    builder.add("filePanel", filePanel);
    builder.add("fileField", fileField);
    builder.add("browseFiles", new BrowseFilesAction(fileField, localRepository, usePreferredPath, dialog));
    builder.add("import", new ImportAction());
    builder.add("close", new AbstractAction(textForCloseButton) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        controller.closeDialog();
      }
    });

    bankDownload = new BankDownloadPanel(dialog, localRepository, directory);
    builder.add("bankDownload", bankDownload.getPanel());

    final HyperlinkHandler hyperlinkHandler = new HyperlinkHandler(directory, dialog);
    hyperlinkHandler.registerLinkAction("openErrorDetails", new Runnable() {
      public void run() {
        showLastException();
      }
    });
    builder.add("hyperlinkHandler", hyperlinkHandler);

    panel = builder.load();
  }

  private void initFileField() {
    fileField.getDocument().addDocumentListener(new AbstractDocumentListener() {
      protected void documentChanged(DocumentEvent e) {
        clearErrorMessage();
      }
    });
  }

  private void clearErrorMessage() {
    importMessage.setText("");
  }

  public void showMessage(String message) {
    showMessage(message, null);
  }

  public void showMessage(String message, Exception exception) {
    lastException = exception;
    this.importMessage.setText(message);
  }

  public JPanel getPanel() {
    return panel;
  }

  public void dispose() {
    builder.dispose();
  }

  public void preselectFiles(List<File> files) {
    StringBuilder builder = new StringBuilder(fileField.getText().trim());
    for (File file : files) {
      if (builder.length() != 0) {
        builder.append(";");
      }
      builder.append(file.getAbsolutePath());
    }
    fileField.setText(builder.toString());
  }

  public void requestFocus() {
    bankDownload.requestFocus();
  }

  private class ImportAction extends AbstractAction {
    public ImportAction() {
      super(Lang.get("import.fileSelection.ok"));
    }

    public void actionPerformed(ActionEvent event) {
      acceptFiles();
    }
  }

  public void acceptFiles() {
    if (!initialFileAccepted()) {
      return;
    }
    controller.doImport();
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
      if (!file.exists()) {
        displayErrorMessage("login.data.file.not.found", fileName);
        return false;
      }
      if (file.isDirectory()) {
        if (strings.length == 1) {
          displayErrorMessage("import.file.is.directory", fileName);
          return false;
        }
      }
      else {
        TypedInputStream inputStream;
        try {
          inputStream = new TypedInputStream(file);
        }
        catch (IOException e) {
          displayErrorMessage("login.data.file.not.found", fileName);
          return false;
        }
        if (inputStream.getType() == null) {
          displayErrorMessage("import.invalid.extension", fileName);
          return false;
        }
      }
    }
    return true;
  }

  private void displayErrorMessage(String key, String... args) {
    this.showMessage("<html><font color=red>" + Lang.get(key, args) + "</font></html>");
  }

  private void showLastException() {
    MessageAndDetailsDialog dialog = new MessageAndDetailsDialog("import.file.error.title",
                                                                 "import.file.error.message",
                                                                 Strings.toString(lastException),
                                                                 directory.get(JFrame.class),
                                                                 directory);
    dialog.show();
  }
}
