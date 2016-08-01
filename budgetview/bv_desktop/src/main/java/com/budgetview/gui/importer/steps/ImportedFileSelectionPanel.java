package com.budgetview.gui.importer.steps;

import com.budgetview.gui.card.NavigationService;
import com.budgetview.gui.components.dialogs.PicsouDialog;
import com.budgetview.gui.importer.MessageHandler;
import com.budgetview.io.importer.utils.TypedInputStream;
import com.budgetview.gui.help.HyperlinkHandler;
import com.budgetview.gui.importer.ImportController;
import com.budgetview.gui.importer.ImportDialog;
import com.budgetview.gui.importer.components.BankDownloadPanel;
import com.budgetview.gui.importer.edition.BrowseFilesAction;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.utils.AbstractDocumentListener;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImportedFileSelectionPanel extends AbstractImportStepPanel implements MessageHandler {

  private LocalGlobRepository localRepository;

  private JPanel panel;
  private JPanel filePanel = new JPanel();
  private GlobsPanelBuilder builder;
  private final JTextField fileField;

  private boolean usePreferredPath;
  private JEditorPane importMessage = new JEditorPane();
  private String lastExceptionDetails;
  private BankDownloadPanel bankDownload;
  private ImportAction importAction;

  public ImportedFileSelectionPanel(PicsouDialog dialog,
                                    String textForCloseButton,
                                    ImportController controller,
                                    boolean usePreferredPath,
                                    LocalGlobRepository localRepository,
                                    Directory directory) {
    super(dialog, textForCloseButton, controller, directory);
    this.usePreferredPath = usePreferredPath;
    this.localRepository = localRepository;
    this.fileField = controller.getFileField();
  }

  public void createPanelIfNeeded() {
    if (builder != null) {
      return;
    }

    builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importFileSelectionPanel.splits",
                                    localRepository, localDirectory);

    initFileField();
    builder.add("importMessage", importMessage);
    builder.add("filePanel", filePanel);
    builder.add("fileField", fileField);
    importAction = new ImportAction();
    builder.add("browseFiles", new BrowseFilesAction(importAction, fileField, localRepository, usePreferredPath, dialog));
    builder.add("import", importAction);
    updateImportAction();
    builder.add("close", new AbstractAction(textForCloseButton) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        controller.closeDialog();
      }
    });

    bankDownload = new BankDownloadPanel(dialog, controller, localRepository, localDirectory);
    builder.add("bankDownload", bankDownload.getPanel());

    final HyperlinkHandler hyperlinkHandler = new HyperlinkHandler(localDirectory);

    hyperlinkHandler.registerLinkAction("openErrorDetails", new Runnable() {
      public void run() {
        ImportDialog.showLastException(ImportedFileSelectionPanel.this.lastExceptionDetails,
                                       ImportedFileSelectionPanel.this.localDirectory);
      }
    });
    builder.add("hyperlinkHandler", hyperlinkHandler);

    builder.add("manualEntry", new ShowManualEntryAction());

    panel = builder.load();
  }

  private void initFileField() {
    fileField.getDocument().addDocumentListener(new AbstractDocumentListener() {
      protected void documentChanged(DocumentEvent e) {
        clearErrorMessage();
        updateImportAction();
      }
    });
  }

  private void updateImportAction() {
    importAction.setEnabled(Strings.isNotEmpty(fileField.getText()));
  }

  private void clearErrorMessage() {
    importMessage.setText("");
  }

  public void showFileErrorMessage(String message) {
    showFileErrorMessage(message, null);
  }

  public void showFileErrorMessage(String message, String details) {
    createPanelIfNeeded();
    lastExceptionDetails = details;
    this.importMessage.setText(message);
  }

  public JPanel getPanel() {
    createPanelIfNeeded();
    return panel;
  }

  public void dispose() {
    if (builder != null) {
      builder.dispose();
      builder = null;
      bankDownload.dispose();
      bankDownload = null;
    }
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
    createPanelIfNeeded();
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
    createPanelIfNeeded();
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

    String[] strings = path.trim().split(";");
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
    this.showFileErrorMessage("<html><font color=red>" + Lang.get(key, args) + "</font></html>");
  }

  private class ShowManualEntryAction extends AbstractAction {

    private ShowManualEntryAction() {
      super(Lang.get("import.fileSelection.manualEntry.action"));
    }

    public void actionPerformed(ActionEvent e) {
      controller.complete();
      controller.closeDialog();
      localDirectory.get(NavigationService.class).highlightTransactionCreation();
    }
  }
}
