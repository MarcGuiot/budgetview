package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
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
  private MenuItem preferencesMenu;
  private MenuItem undoMenu;
  private MenuItem redoMenu;
  private MenuItem dumpMenu;
  private MenuItem checkMenu;
  public static final String DEFAULT_ACCOUNT_NUMBER = "11111";
  public static final String DEFAULT_ACCOUNT_NAME = "Account n. 11111";
  private Window window;

  public OperationChecker(Window window) {
    this.window = window;
    MenuItem fileMenu = window.getMenuBar().getMenu("File");
    importMenu = fileMenu.getSubMenu("Import");
    exportMenu = fileMenu.getSubMenu("Export");
    preferencesMenu = fileMenu.getSubMenu("Preferences");

    MenuItem editMenu = window.getMenuBar().getMenu("Edit");
    undoMenu = editMenu.getSubMenu("Undo");
    redoMenu = editMenu.getSubMenu("Redo");
    dumpMenu = editMenu.getSubMenu("Dump");
    checkMenu = editMenu.getSubMenu("check");
  }

  public ImportChecker openImportDialog() {
    Window dialog = WindowInterceptor.getModalDialog(importMenu.triggerClick());
    return new ImportChecker(dialog);
  }

  public void importOfxFile(String name) {
    importFile(new String[]{name}, null, null);
  }

  public void importQifFile(String file, String bank) {
    importFile(new String[]{file}, bank, null);
  }

  public void importQifFile(String file, String bank, Double amount) {
    importFile(new String[]{file}, bank, amount);
  }

  public void importQifFiles(String bank, String... files) {
    importFile(files, bank, null);
  }

  private void importFile(final String[] fileNames, final String bank, final Double amount) {
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

  public void check() {
    checkMenu.click();
  }
}
