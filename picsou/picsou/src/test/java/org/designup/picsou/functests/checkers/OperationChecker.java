package org.designup.picsou.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.MenuItem;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class OperationChecker {
  private MenuItem importMenu;
  private MenuItem exportMenu;
  private MenuItem undoMenu;
  private MenuItem redoMenu;
  public static final String DEFAULT_ACCOUNT_NUMBER = "11111";

  public OperationChecker(Window window) {
    MenuItem fileMenu = window.getMenuBar().getMenu("File");
    importMenu = fileMenu.getSubMenu("Import");
    exportMenu = fileMenu.getSubMenu("Export");

    MenuItem editMenu = window.getMenuBar().getMenu("Edit");
    undoMenu = editMenu.getSubMenu("Undo");
    redoMenu = editMenu.getSubMenu("Redo");
  }

  public void importOfxFile(String name) {
    importFile(new String[]{name}, null);
  }

  public void importQifFile(String file, String bank) {
    importFile(new String[]{file}, bank);
  }

  public void importQifFiles(String bank, String... files) {
    importFile(files, bank);
  }

  private void importFile(final String[] fileNames, final String bank) {
    WindowInterceptor
      .init(importMenu.triggerClick())
      .process(new WindowHandler() {

        public Trigger process(Window importDialog) throws Exception {

          WindowInterceptor.init(importDialog.getButton("Browse").triggerClick())
            .process(FileChooserHandler.init().select(fileNames))
            .run();

          importDialog.getButton("Import").click();
          if (importDialog.getInputTextBox("number").isEditable().isTrue()) {
            importDialog.getInputTextBox("number").setText(DEFAULT_ACCOUNT_NUMBER);
            importDialog.getComboBox("accountBank").select(bank);
          }
          Button okButton = importDialog.getButton("OK");
          for (int i = 0; i < fileNames.length - 1; i++) {
            okButton.click();
          }

          return okButton.triggerClick();
        }
      })
      .run();
  }

  public void exportFile(String name) {
    WindowInterceptor
      .init(exportMenu.triggerClick())
      .process(FileChooserHandler.init().select(name))
      .run();
  }

  public Trigger getImportTrigger() {
    return importMenu.triggerClick();
  }

  public Trigger getExportTrigger() {
    return exportMenu.triggerClick();
  }

  public void undo() {
    undoMenu.click();
  }

  public void checkUndoAvailable() {
    UISpecAssert.assertTrue(undoMenu.isEnabled());
  }

  public void checkUndoNotAvailable() {
    UISpecAssert.assertFalse(undoMenu.isEnabled());
  }

  public void redo() {
    redoMenu.click();
  }

  public void checkRedoAvailable() {
    UISpecAssert.assertTrue(redoMenu.isEnabled());
  }

  public void checkRedoNotAvailable() {
    UISpecAssert.assertFalse(redoMenu.isEnabled());
  }


}
