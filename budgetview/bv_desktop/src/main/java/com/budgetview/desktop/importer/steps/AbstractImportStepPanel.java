package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.components.dialogs.MessageDialog;
import com.budgetview.desktop.components.dialogs.MessageType;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.desktop.importer.ImportStepPanel;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public abstract class AbstractImportStepPanel implements ImportStepPanel, Disposable {
  protected final PicsouDialog dialog;
  protected final ImportController controller;
  protected final Directory localDirectory;
  private GlobsPanelBuilder builder;
  private JPanel panel;

  public AbstractImportStepPanel(PicsouDialog dialog, ImportController controller, Directory localDirectory) {
    this.dialog = dialog;
    this.controller = controller;
    this.localDirectory = localDirectory;
  }

  public void showFileErrorMessage(String message) {
    MessageDialog.show("import.error.title", MessageType.ERROR, dialog, localDirectory, "import.error.message", message);
  }

  public void showFileErrorMessage(String message, String details) {
    MessageDialog.show("import.error.title", MessageType.ERROR, dialog, localDirectory, "import.error.messageWithDetails", message, details);
  }

  public void createPanelIfNeeded() {
    getPanel();
  }

  public JPanel getPanel() {
    if (panel == null) {
      if (builder == null) {
        builder = createPanelBuilder();
      }
      panel = builder.load();
    }
    return panel;
  }

  protected String getNextLabel() {
    return Lang.get("import.next");
  }

  protected String getCancelLabel() {
    return Lang.get("import.cancel");
  }

  protected String getCloseLabel() {
    return Lang.get("import.close");
  }

  protected abstract GlobsPanelBuilder createPanelBuilder();

  public void dispose() {
    if (builder != null) {
      builder.dispose();
      builder = null;
    }
  }
}
