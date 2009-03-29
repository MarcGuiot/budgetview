package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class OperationChecker {
  private MenuItem importMenu;
  private MenuItem exportMenu;
  private MenuItem backupMenu;
  private MenuItem restoreMenu;
  private MenuItem preferencesMenu;
  private MenuItem undoMenu;
  private MenuItem redoMenu;
  private MenuItem dumpMenu;
  public static final String DEFAULT_ACCOUNT_NUMBER = "11111";
  public static final String DEFAULT_ACCOUNT_NAME = "Account n. 11111";
  private Window window;

  public static OperationChecker init(Window window) {
    return new OperationChecker(window);
  }

  public OperationChecker(Window window) {
    this.window = window;
    MenuItem fileMenu = window.getMenuBar().getMenu("File");
    importMenu = fileMenu.getSubMenu("Import");
    exportMenu = fileMenu.getSubMenu("Export");
    backupMenu = fileMenu.getSubMenu("Backup");
    restoreMenu = fileMenu.getSubMenu("Restore");
    preferencesMenu = fileMenu.getSubMenu("Preferences");

    MenuItem editMenu = window.getMenuBar().getMenu("Edit");
    undoMenu = editMenu.getSubMenu("Undo");
    redoMenu = editMenu.getSubMenu("Redo");
    dumpMenu = editMenu.getSubMenu("Dump");
  }

  public ImportChecker openImportDialog() {
    return ImportChecker.open(importMenu.triggerClick());
  }

  public void importOfxFile(String name) {
    importFile(new String[]{name}, null, null, null);
  }

  public void importQifFile(String file, String bank) {
    importFile(new String[]{file}, bank, null, null);
  }

  public void importQifFile(String file, String bank, Double amount) {
    importFile(new String[]{file}, bank, amount, null);
  }

  public void importQifFile(String file, String bank, String targetAccount) {
    importFile(new String[]{file}, bank, null, targetAccount);
  }

  public void importQifFiles(String bank, String... files) {
    importFile(files, bank, null, null);
  }

  private void importFile(final String[] fileNames, final String bank, final Double amount, final String targetAccount) {
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
          if (targetAccount != null) {
            importDialog.getComboBox("accountCombo").select(targetAccount);
          }
          Button okButton = importDialog.getButton(Lang.get("import.ok"));
          for (int i = 0; i < fileNames.length - 1; i++) {
            okButton.click();
          }
          if (amount != null) {
            Window window = WindowInterceptor.getModalDialog(okButton.triggerClick());
            BalanceEditionChecker balance = new BalanceEditionChecker(window);
            balance.setAmountWithoutEnter(amount);
            return balance.triggerValidate();
          }
          return okButton.triggerClick();
        }
      })
      .run();

  }

  public void exportOfxFile(String name) {
    WindowInterceptor
      .init(exportMenu.triggerClick())
      .processWithButtonClick("OK")
      .process(FileChooserHandler.init().select(name))
      .run();
  }

  public String backup(String dirName) {
    final String[] fileName = new String[1];
    WindowInterceptor
      .init(backupMenu.triggerClick())
      .process(FileChooserHandler.init().select(dirName))
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          UISpecAssert.assertTrue(window.getTextBox("message").textContains("Backup done in file"));
          TextBox box = window.getInputTextBox("file");
          fileName[0] = box.getText();
          return window.getButton().triggerClick();
        }
      })
      .run();
    return fileName[0];
  }

  public void restore(String name) {
    WindowInterceptor
      .init(restoreMenu.triggerClick())
      .process(FileChooserHandler.init().select(name))
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          UISpecAssert.assertTrue(window.getTextBox("message").textContains("Restore done"));
          return window.getButton().triggerClick();
        }
      })
      .run();
  }

  public Trigger getImportTrigger() {
    return importMenu.triggerClick();
  }

  public Trigger getExportTrigger() {
    return exportMenu.triggerClick();
  }

  public PreferencesChecker openPreferences() {
    return new PreferencesChecker(WindowInterceptor.getModalDialog(preferencesMenu.triggerClick()));
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

  public void exit() {
    window.getMenuBar().getMenu("File").getSubMenu("Exit").click();
  }

  public HelpChecker openHelp() {
    return HelpChecker.open(window.getMenuBar().getMenu("Help").getSubMenu("Index").triggerClick());
  }

  public AboutChecker openAbout() {
    return AboutChecker.open(window.getMenuBar().getMenu("Help").getSubMenu("About").triggerClick());
  }

  public void dump() {
    dumpMenu.click();
  }
}
