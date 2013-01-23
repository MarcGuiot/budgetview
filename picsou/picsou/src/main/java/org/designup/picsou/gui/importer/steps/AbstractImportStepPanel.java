package org.designup.picsou.gui.importer.steps;

import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.importer.ImportController;
import org.designup.picsou.gui.importer.ImportStepPanel;
import org.globsframework.utils.directory.Directory;

public abstract class AbstractImportStepPanel implements ImportStepPanel {
  protected final PicsouDialog dialog;
  protected final String textForCloseButton;
  protected final ImportController controller;
  protected final Directory localDirectory;

  public AbstractImportStepPanel(PicsouDialog dialog, String textForCloseButton, ImportController controller, Directory localDirectory) {
    this.dialog = dialog;
    this.textForCloseButton = textForCloseButton;
    this.controller = controller;
    this.localDirectory = localDirectory;
  }

  public void showFileErrorMessage(String message) {
    MessageDialog.show("import.error.title", dialog, localDirectory, "import.error.message", message);
  }

  public void showFileErrorMessage(String message, String details) {
    MessageDialog.show("import.error.title", dialog, localDirectory, "import.error.messageWithDetails", message, details);
  }

  protected abstract void createPanelIfNeeded();
}